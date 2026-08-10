# 🚀 Guia de Release e Publicação

Este documento descreve o procedimento padrão para gerar uma nova versão do app **Meus Remedinhos** para envio ao Google Play Console.

---

## 🛠 Procedimento Passo a Passo

Siga estas etapas rigorosamente para garantir a consistência do versionamento e a integridade do binário.

### 1. Preparação e Testes
Antes de qualquer bump, garanta que o código está estável e todos os testes passam.
```bash
./gradlew clean testDebugUnitTest connectedAndroidTest
```

### 2. Bump de Versão
No arquivo `app/build.gradle.kts`, localize o bloco `defaultConfig` e atualize:
- **`versionCode`**: Incremente em +1 (ex: de 13 para 14).
- **`versionName`**: Atualize seguindo o versionamento semântico (ex: `3.6.0` para `4.0.0`).

### 3. Geração do Bundle (AAB)
Gere o binário otimizado para a Play Store:
```bash
./gradlew clean :app:bundleRelease
```
O arquivo será gerado em: `app/build/outputs/bundle/release/app-release.aab`.

### 4. Versionamento (Commit e Tag)
Após gerar o AAB e validar localmente, realize o commit das alterações de versão e crie a tag de release.

**Commit:**
```bash
git add .
git commit -m "Release version <versionName> (code <versionCode>)"
```

**Tag:**
```bash
git tag -a v<versionName> -m "Release version <versionName>"
```

### 5. Sincronização Remota
Envie o commit e a tag para o repositório principal:
```bash
git push origin main
git push origin v<versionName>
```

---

## 🌍 Publicação na Play Store

Após gerar o arquivo `.aab`, siga os passos abaixo no [Google Play Console](https://play.google.com/console/):

1.  **Acesse o App:** Selecione "Meus Remedinhos" no painel.
2.  **Produção:** No menu lateral, vá em "Produção" -> "Versões".
3.  **Criar Nova Versão:** Clique em "Criar nova versão" no canto superior direito.
4.  **Upload:** Arraste o arquivo `app-release.aab` para a área de upload.
5.  **Notas de Versão:** Descreva as novidades no campo `pt-BR`.
6.  **Revisão:** Clique em "Próximo", revise os detalhes e clique em "Iniciar lançamento para Produção".

> [!TIP]
> Para atualizações menores, considere usar o canal de **Teste Interno** antes de promover para produção.

---

## 📋 Resumo para o AI Agent

Quando solicitado para "fazer um release", o Agent deve:
1. Executar os testes unitários.
2. Realizar o commit de todas as mudanças de código pendentes com uma mensagem descritiva.
3. Ler o `versionCode` e `versionName` atual no `build.gradle.kts`.
4. Incrementar a versão (Sugerir `minor` ou aguardar confirmação).
5. Atualizar o arquivo `build.gradle.kts`.
6. Gerar o AAB via `:app:bundleRelease`.
7. Realizar o commit com a mensagem padrão de release.
8. Criar a tag seguindo o padrão `vX.Y.Z`.
9. Notificar o caminho do arquivo `.aab` gerado e confirmar o sucesso.

> [!IMPORTANT]
> Nunca realize o push sem a confirmação final do usuário sobre os novos valores de versão.
