# 👨‍💻 Developer Guide: Meus Remedinhos (Native)

> Complete technical reference for developers: setup, project structure, coding patterns, and debugging.

---

## 1. Local Development Setup

### 1.1 Prerequisites
- **JDK 17** (OpenJDK recommended).
- **Android Studio Ladybug (2024.1.1)** or later.
- **Android SDK API 34+** (Android 14).
- **Gradle 8.x** (bundled with the project).

### 1.2 Installation & Run
1.  **Clone** the repository and navigate to the `Kotlin` directory.
2.  **Open** the project in Android Studio.
3.  **Sync Gradle** to download all dependencies.
4.  **Run** the `:app` module on an emulator (API 34) or a physical device.

### 1.3 Linux / Distrobox Environment
For Linux developers using Distrobox:
```bash
distrobox enter ubuntu22-android
./gradlew assembleDebug
```
Refer to [docs/DISTROBOX_GUIDE.md](DISTROBOX_GUIDE.md) for detailed setup.

---

## 2. Project Structure

```text
app/src/main/java/com/franciscokahil/appMeusRemedinhos/
│
├── ui/ ........................ Jetpack Compose screens and ViewModels
│   ├── dashboard/ ............. Main schedule list and editing logic
│   ├── onboarding/ ............ First-run experience
│   └── theme/ ................. Material Design 3 theme definitions
│
├── data/ ...................... Persistence and Repository layer
│   ├── local/ ................. Room database, DAOs, and Entities
│   └── repository/ ............ Data orchestration and side-effects
│
├── background/ ................ Alarm scheduling and notifications
│   ├── AlarmScheduler.kt ...... Logic for system alarms
│   └── NotificationHelper.kt .. Dispatching system notifications
│
└── widget/ .................... Jetpack Glance home screen widgets
```

---

## 3. Architecture & Patterns

### 3.1 MVVM (Model-View-ViewModel)
The project strictly follows MVVM. ViewModels expose state via `StateFlow` to Compose screens.
- **Immutability**: Data classes (Entities/Models) should be immutable. Use `.copy()` for updates.
- **Main Safety**: Repositories must ensure operations are main-safe using `Dispatchers.IO` for database work.

### 3.2 Reactive UI
UI automatically recomposes when the underlying Room database changes, thanks to `Flow` emissions from DAOs being collected as `StateFlow` in ViewModels.

### 3.3 Dependency Injection (Manual)
For simplicity, we use manual DI via ViewModel Factories. Check `DashboardViewModelFactory.kt` for implementation details.

---

## 4. Key Development Rules

- **Resource-Only Strings**: Never hardcode UI text. Use `res/values/strings.xml` for all strings to support localization (currently PT and EN).
- **Trailing Commas**: Always use trailing commas in parameters and collection literals for cleaner git diffs.
- **Pure Composables**: Keep UI components decoupled from business logic. Pass data and callbacks (hoisting state).
- **KSP over KAPT**: Use KSP for Room and other annotation processing tasks.

---

## 5. Testing
The project prioritizes a robust testing suite:
- **Unit Tests**: Test logic in ViewModels and Repositories.
- **Instrumented Tests**: Test UI, Room DAO, and deep-linking.

Refer to [**docs/TESTING.md**](TESTING.md) for detailed execution commands.

---

## 6. Debugging Tips
- **Logcat**: Filter by package name `com.franciscokahil.appMeusRemedinhos`.
- **Database**: Use Android Studio's **App Inspection** tool to view the live SQLite database.
- **Alarms**: Use `adb shell dumpsys alarm | grep appMeusRemedinhos` to verify scheduled triggers.
