import os
import sys
from google import genai

api_key = os.environ.get("AI_API_KEY")
issue_title = os.environ.get("ISSUE_TITLE", "")
issue_body = os.environ.get("ISSUE_BODY", "")

if not api_key:
    print("Erro: AI_API_KEY não configurada.")
    sys.exit(1)

# O SDK moderno instancia via Client
client = genai.Client(api_key=api_key)

prompt = f"""
Você é um agente de desenvolvimento de software autônomo.
Analise a seguinte Issue do GitHub e forneça um plano ou as correções de código necessárias.

Título da Issue: {issue_title}
Descrição da Issue: {issue_body}

Seja direto e indique quais arquivos devem ser modificados e como.
"""

response = client.models.generate_content(
    model="gemini-2.5-pro",
    contents=prompt,
)

print("--- Resposta do Agente ---")
print(response.text)
