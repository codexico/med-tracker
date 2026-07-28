# 📦 Guia de Ambiente (Distrobox + Ubuntu)

Este projeto está configurado para ser desenvolvido em um ambiente isolado usando **Distrobox** (container `ubuntu22-android`).

> [!NOTE]
> As variáveis `ANDROID_HOME` e `PATH` já foram configuradas no seu `~/.bashrc` dentro do container para apontar para o SDK no host.

## 🛠 Caminhos e Variáveis

- **Nome do Container:** `ubuntu22-android`
- **Home do Distrobox:** `/var/home/fk/distrobox_home/ubuntu-android`
- **Home do Host:** `/var/home/fk`
- **Android SDK (Host):** `/var/home/fk/Android/Sdk`
- **Android Studio:** `/var/home/fk/distrobox_home/ubuntu-android/opt/android-studio/bin/studio.sh`

## 🚀 Como Abrir o Android Studio

Para abrir o Android Studio de dentro do seu container Distrobox:

```bash
distrobox-enter -n ubuntu22-android -- /var/home/fk/distrobox_home/ubuntu-android/opt/android-studio/bin/studio.sh
```

## 📱 Como Rodar no Emulador

### Via Android Studio
1. Abra o projeto no Android Studio.
2. Certifique-se de que o SDK Path está configurado como `/var/home/fk/Android/Sdk` em **File > Settings > Languages & Frameworks > Android SDK**.
3. Abra o **Device Manager** e inicie seu emulador.
4. Clique no botão **Run (Play)**.

### Via Terminal (Dentro do Distrobox)
Como o PATH já está configurado no `.bashrc`, basta entrar no container e rodar o comando:

```bash
# Entre no container
distrobox-enter -n ubuntu22-android

# No diretório do projeto:
./gradlew installDebug
```

---

## 💡 Dica de Produtividade: Alias no Host

Para evitar ter que digitar `distrobox-enter...` toda vez, você pode adicionar este alias ao seu `~/.bashrc` ou `~/.zshrc` **no seu HOST**:

```bash
alias dbox-gradle='distrobox-enter -n ubuntu22-android -- ./gradlew'
```

Assim, basta rodar `dbox-gradle test` de dentro da pasta do projeto no host.

---

## 🧪 Como Rodar os Testes

### ⚡ Testes Unitários (JVM)
Estes rodam rapidamente sem necessidade de emulador. 

**Importante:** Você deve estar no diretório raiz do projeto ao executar, caso contrário o Gradle não encontrará o build.

```bash
# De dentro da pasta do projeto no host:
distrobox-enter -n ubuntu22-android -- ./gradlew testDebugUnitTest
```

### 📱 Testes Instrumentados (Android Test)
**Importante:** O emulador deve estar aberto e visível para o ADB.

```bash
# De dentro da pasta do projeto no host:
distrobox-enter -n ubuntu22-android -- ./gradlew connectedAndroidTest
```

---

## 🛠 Solução de Problemas (Troubleshooting)

### Erro: "Build-tool 36.0.0 is missing AAPT"
Este erro geralmente ocorre porque o container Ubuntu não possui as bibliotecas de 32 bits ou dependências de C++ necessárias para rodar os binários do SDK.

Para corrigir, entre no seu container e instale as dependências:

```bash
distrobox-enter -n ubuntu22-android
sudo apt update
sudo apt install lib32z1 libncurses5 libstdc++6:i386 zlib1g:i386
```

### Erro: "Directory ... does not contain a Gradle build"
Isso acontece se você rodar o comando fora da pasta do projeto. Certifique-se de dar `cd` para `/var/home/fk/.gemini/antigravity/scratch/med-tracker/Kotlin` antes de rodar o `distrobox-enter`.

---

## ⚠️ Observações Importantes
- **KVM:** Para o emulador funcionar dentro do Distrobox, seu usuário deve estar nos grupos `kvm`, `libvirt` e `render` no host.
- **ADB:** Se o Android Studio no Distrobox não encontrar o dispositivo, tente reiniciar o servidor adb no host (`adb kill-server`) e depois no container.
