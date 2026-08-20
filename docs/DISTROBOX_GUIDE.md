# 📦 Environment Guide: Distrobox + Ubuntu

This project is configured for development within an isolated **Distrobox** container (`ubuntu22-android`). This ensures build reproducibility and avoids polluting your host system.

---

## 🛠 Paths and Variables

- **Container Name:** `ubuntu22-android`
- **Distrobox Home:** `/var/home/fk/distrobox_home/ubuntu-android`
- **Host Home:** `/var/home/fk`
- **Android SDK (Host):** `/var/home/fk/Android/Sdk`
- **Android Studio Executable:** `/var/home/fk/distrobox_home/ubuntu-android/opt/android-studio/bin/studio.sh`

---

## 🚀 How to Open Android Studio

To launch Android Studio from within your Distrobox container:

```bash
distrobox-enter -n ubuntu22-android -- /var/home/fk/distrobox_home/ubuntu-android/opt/android-studio/bin/studio.sh
```

*Note: If you use Jetbrains Toolbox, launch the toolbox first:*
```bash
distrobox-enter -n ubuntu22-android -- ./opt/jetbrains-toolbox/bin/jetbrains-toolbox
```

---

## 📱 Running the App

### Via Android Studio (Recommended)
1.  Open the project in Android Studio.
2.  Verify the **SDK Path** in *File > Settings > Languages & Frameworks > Android SDK*. It should be `/var/home/fk/Android/Sdk`.
3.  Open the **Device Manager** and start your emulator.
4.  Click the **Run** button.

### Via Terminal (Inside Distrobox)
```bash
distrobox-enter -n ubuntu22-android
# Inside the project directory:
./gradlew installDebug
```

---

## 💡 Productivity Tip: Host Aliases

Add this alias to your host's `~/.bashrc` or `~/.zshrc` to run Gradle commands easily:

```bash
alias dbox-gradle='distrobox-enter -n ubuntu22-android -- ./gradlew'
```

Usage: `dbox-gradle testDebugUnitTest`.

---

## 🛠 Troubleshooting

### Error: "Build-tool missing AAPT"
This usually happens due to missing 32-bit libraries in the Ubuntu container. Fix it by running:
```bash
distrobox-enter -n ubuntu22-android
sudo apt update
sudo apt install lib32z1 libncurses5 libstdc++6:i386 zlib1g:i386
```

### Emulator Connectivity
If the emulator is not detected, restart the ADB server on your **host** first:
```bash
adb kill-server && adb start-server
```
Then restart it inside the container.
