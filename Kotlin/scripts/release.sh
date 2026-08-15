#!/bin/bash

# Script de Release Automatizado - Meus Remedinhos
# Uso: ./scripts/release.sh [patch|minor|major]

set -e # Aborta em caso de erro simples não tratado

TYPE="${1:-patch}"
BUILD_GRADLE="app/build.gradle.kts"

echo "🚀 Iniciando processo de release ($TYPE)..."

# 1. Rodar Testes
echo "🧪 Rodando testes unitários..."
if ! ./gradlew testDebugUnitTest; then
    echo "❌ Erro: Testes falharam. Release cancelado."
    exit 1
fi

# 2. Check de alterações pendentes interativo
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️  Existem alterações pendentes no repositório."
    echo "O que deseja fazer?"
    echo "1) Sair do script para revisar as mudanças"
    echo "2) Commitar as mudanças agora e prosseguir"
    read -r -p "Escolha uma opção [1-2]: " choice

    case "$choice" in
        1)
            echo "🛑 Release cancelado pelo usuário."
            exit 0
            ;;
        2)
            read -r -p "Digite a mensagem do commit: " commit_msg
            if [ -z "$commit_msg" ]; then
                echo "❌ Erro: Mensagem de commit é obrigatória. Release cancelado."
                exit 1
            fi
            git add .
            git commit -m "$commit_msg"
            echo "✅ Alterações commitadas."
            ;;
        *)
            echo "❌ Opção inválida. Release cancelado."
            exit 1
            ;;
    esac
fi

# 3. Bump de Versão
echo "🔢 Atualizando versão no Gradle..."
CURRENT_VERSION=$(grep "versionName =" "$BUILD_GRADLE" | sed 's/.*"\(.*\)".*/\1/')
CURRENT_CODE=$(grep "versionCode =" "$BUILD_GRADLE" | sed 's/.*= //')

# Lógica de incremento (X.Y.Z)
IFS='.' read -r -a ADDR <<< "$CURRENT_VERSION"
MAJOR="${ADDR[0]}"
MINOR="${ADDR[1]}"
PATCH="${ADDR[2]}"

if [ "$TYPE" == "major" ]; then
    MAJOR=$((MAJOR + 1))
    MINOR=0
    PATCH=0
elif [ "$TYPE" == "minor" ]; then
    MINOR=$((MINOR + 1))
    PATCH=0
else
    PATCH=$((PATCH + 1))
fi

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
NEW_CODE=$((CURRENT_CODE + 1))

# Atualizar arquivo (usando sed com aspas para evitar problemas com espaços)
sed -i "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$BUILD_GRADLE"
sed -i "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" "$BUILD_GRADLE"

echo "✅ Versão atualizada: $CURRENT_VERSION ($CURRENT_CODE) -> $NEW_VERSION ($NEW_CODE)"

# 4. Build de Produção
echo "🏗️ Gerando bundle de produção (AAB)..."
if ! ./gradlew clean :app:bundleRelease; then
    echo "❌ Erro: Falha no build. Revertendo mudanças..."
    git checkout "$BUILD_GRADLE"
    exit 1
fi

# 5. Finalizar Git (Commit, Tag e Push)
echo "💾 Finalizando Git..."
git add "$BUILD_GRADLE"
git commit -m "Release v$NEW_VERSION ($NEW_CODE)"
git tag -a "v$NEW_VERSION" -m "Release version $NEW_VERSION"

echo "☁️ Sincronizando com repositório remoto..."
git push origin main --follow-tags

echo "🎉 Release $NEW_VERSION concluído com sucesso!"
