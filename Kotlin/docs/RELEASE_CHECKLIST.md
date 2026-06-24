# 🚀 Checklist de Publicação (Google Play Store)

Siga estas etapas antes de enviar o aplicativo para produção.

---

## 🛠 Configuração Técnica

- [ ] **Chave de Assinatura (Keystore):**
    - Gere uma chave `.jks` no Android Studio (`Build > Generate Signed Bundle / APK`).
    - Guarde o arquivo e as senhas em local seguro!
- [ ] **Configuração do Gradle:**
    - Atualize o `build.gradle.kts` para usar a sua chave de assinatura em vez da `debugConfig`.
    - Verifique se `versionCode` foi incrementado (em relação à versão anterior na Play Store).
- [ ] **ProGuard / R8:**
    - Ative `isMinifyEnabled = true` no `build.gradle.kts`.
    - Teste o APK de release exaustivamente, pois a ofuscação pode quebrar o Room ou Reflexão se as regras em `proguard-rules.pro` estiverem incompletas.

## 🎨 Ativos e Identidade

- [ ] **Ícone do App:** Verifique se o ícone adaptativo (`res/mipmap-anydpi-v26/ic_launcher.xml`) está correto.
- [ ] **Logo do Header:** Verifique se o logo dinâmico no dashboard está com o contraste correto.

## 📝 Conteúdo e Localização

- [ ] **Internacionalização:** Atualmente suportamos Português (padrão) e Inglês. Verifique se novas strings foram traduzidas em `values-en/strings.xml`.
- [ ] **Políticas de Privacidade:** Crie uma URL com a política de privacidade (exigido pelo Google).

## 🧪 Qualidade e Testes

- [ ] **Testes de Regressão:** Execute todos os testes unitários e instrumentados.
    - `./gradlew testDebugUnitTest connectedAndroidTest`
- [ ] **Teste em Dispositivo Real:** O comportamento do `AlarmManager` pode variar entre fabricantes (Samsung, Xiaomi, etc.). Teste em dispositivos físicos se possível.
- [ ] **Crashlytics / Analytics:** Considere adicionar Firebase Crashlytics para monitorar erros em produção.

---

## 📦 Gerando o Pacote Final

Para gerar o arquivo `.aab` (Android App Bundle) para a Play Store:

```bash
./gradlew bundleRelease
```

O arquivo estará em: `app/build/outputs/bundle/release/app-release.aab`
