# 🚀 Guia de Release e Publicação

Este documento descreve o procedimento padrão para gerar uma nova versão do app **Meus Remedinhos**.

---

## 📦 Ambiente de Build (Distrobox)

Este projeto utiliza um ambiente isolado para build. **Todas as etapas de build e teste devem ser executadas dentro do container Distrobox.**

**Como entrar no ambiente:**
```bash
distrobox enter ubuntu22-android
```
*(Para mais detalhes, veja o [Guia do Distrobox](DISTROBOX_GUIDE.md))*

---

## 🛠 Script de Automação (Recomendado)

Para facilitar o processo, existe um script que executa todas as etapas abaixo automaticamente.

**Uso:**
```bash
./scripts/release.sh [patch|minor|major]
```
*(Se não especificado, o padrão é `patch`)*

---

## 📋 Procedimento Detalhado (Manual ou via Script)

As etapas a seguir são executadas na ordem correta para garantir a integridade do release:

### 1. Testes Automatizados
O primeiro passo de qualquer release é garantir que nada foi quebrado.
```bash
./gradlew testDebugUnitTest
```

### 2. Commit de Alterações Pendentes
Nenhuma alteração deve ficar de fora do binário gerado.
```bash
git add .
git commit -m "feat/fix: descrição das mudanças antes do release"
```

### 3. Bump de Versão (Gradle)
No arquivo `app/build.gradle.kts`, atualize o bloco `defaultConfig`:
- **`versionCode`**: Incremente em +1 obrigatoriamente.
- **`versionName`**: Atualize conforme o tipo de release (Major, Minor ou Patch).

### 4. Build para Produção
Gere o binário de produção (Bundle) para a Play Store.
```bash
./gradlew clean :app:bundleRelease
```

### 5. Commit de Versão e Tagging
Realize o commit apenas da alteração no `build.gradle.kts` e crie a tag Git correspondente.
```bash
git add app/build.gradle.kts
git commit -m "Release v<versionName> (<versionCode>)"
git tag -a v<versionName> -m "Release version <versionName>"
```

### 6. Sincronização do Repositório (Push)
Envie todos os commits e a tag para o servidor remoto.
```bash
git push origin main --follow-tags
```
```bash
gh release create v<versionName> --generate-notes
```

---

## 📋 Instruções para o AI Agent

Quando o usuário solicitar um **"Release"**, o Agent deve agir como um pair programmer sênior e seguir este protocolo, **sempre garantindo que os comandos de build rodem no ambiente Distrobox**:

1.  **Ambiente**: Verificar se o comando está sendo executado dentro do container. Se estiver fora, prefixar comandos Gradle com `distrobox-enter -n ubuntu22-android --`.
2.  **Tests**: Rodar os testes unitários.
3.  **Check-in**: Verificar se há arquivos não commitados e realizar o commit.
4.  **Version Bump**: Incrementar `versionCode` (+1) e `versionName` (Minor ou Patch).
5.  **Build**: Executar o build do bundle.
6.  **Finalize Git**: Commitar o bump, criar a tag e executar o `push` com `--follow-tags`.
7.  **Relatório**: Informar o sucesso, a nova versão e o local do arquivo `.aab`.

> [!IMPORTANT]
> O processo de release só é considerado completo após o **Push** bem-sucedido para o repositório remoto.

---

## 🌍 Publicação na Play Store

1.  O arquivo gerado estará em: `app/build/outputs/bundle/release/app-release.aab`.
2.  Faça o upload manualmente no [Google Play Console](https://play.google.com/console/).
