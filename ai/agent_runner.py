import json
import os
import sys
from pydantic import BaseModel
from google import genai
from google.genai import types
from google.genai.errors import ServerError

api_key = os.environ.get("AI_API_KEY")
issue_title = os.environ.get("ISSUE_TITLE", "")
issue_body = os.environ.get("ISSUE_BODY", "")
issue_number = os.environ.get("ISSUE_NUMBER", "0")

if not api_key:
    print("Error: AI_API_KEY is missing.")
    sys.exit(1)

client = genai.Client(api_key=api_key)

def get_repo_structure():
    file_list = []
    ignored = {
        ".git", ".github", ".gradle", ".idea", 
        "build", "node_modules", "captures"
    }
    for root, dirs, files in os.walk("."):
        dirs[:] = [d for d in dirs if d not in ignored]
        for f in files:
            if f.endswith((".kt", ".java", ".xml", ".gradle.kts", ".gradle", ".properties")):
                path = os.path.relpath(os.path.join(root, f), ".")
                file_list.append(path)
    return "\n".join(file_list[:150])

class FileChange(BaseModel):
    filepath: str
    content: str

class AgentResponse(BaseModel):
    explanation: str
    changes: list[FileChange]

def gerar_conteudo_com_seguranca(prompt_text):
    modelos = ["gemini-3.8-flash", "gemini-3.6-flash", "gemini-3.7-flash", "gemini-3.1-pro"]
    
    configuracao = types.GenerateContentConfig(
        response_mime_type="application/json",
        response_schema=AgentResponse,
    )
    
    for modelo in modelos:
        try:
            print(f"Tentando rodar com o modelo: {modelo}")
            response = client.models.generate_content(
                model=modelo,
                contents=prompt_text,
                config=configuracao
            )
            return response
        except ServerError as e:
            # Verifica se é erro 503 (Unavailable)
            if "503" in str(e) or "UNAVAILABLE" in str(e):
                print(f"O modelo {modelo} está sobrecarregado (503). Tentando o próximo...")
                continue
            # Se for outro tipo de erro (ex: 400 Bad Request, permissão), interrompe e mostra o erro
            raise e
            
    raise Exception("Todos os modelos disponíveis falharam devido à alta demanda.")

context_tree = get_repo_structure()

prompt = f"""
You are an expert Android software engineer specializing in Kotlin.
Your task is to resolve the following GitHub issue by modifying or creating the required source files.

### Project File Structure:
{context_tree}

### Issue #{issue_number}: {issue_title}
### Description:
{issue_body}

### Guidelines:
1. Ensure the Kotlin code adheres to standard Android conventions.
2. Avoid deleting critical logic unless required by the issue description.
3. Provide the full file content for each modified or newly created file, not diffs or placeholders.
4. Keep the explanation concise and clear for the Pull Request body.
"""

# Executa a função com o fallback
response = gerar_conteudo_com_seguranca(prompt)

data = json.loads(response.text)
print(f"Summary of changes: {data.get('explanation')}")

for change in data.get("changes", []):
    path = change["filepath"]
    os.makedirs(os.path.dirname(path) or ".", exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(change["content"])
    print(f"Updated: {path}")

with open("pr_description.txt", "w", encoding="utf-8") as f:
    f.write(f"Resolves #{issue_number}\n\n" + data.get("explanation", ""))
