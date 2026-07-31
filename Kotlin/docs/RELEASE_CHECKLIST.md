# 🚀 Release Checklist (Google Play Store)

> Follow these steps before pushing the application to production.

---

## 🛠 Technical Configuration

- [ ] **Signing Key (Keystore)**:
    - Ensure you have the `.jks` file and credentials.
    - Check that the `release` block in `app/build.gradle.kts` points to the correct signing config.
- [ ] **Version Bump**:
    - Increment `versionCode` by 1.
    - Update `versionName` to the new semantic version (e.g., `3.4.0`).
- [ ] **ProGuard / R8**:
    - Verify `isMinifyEnabled = true` is active for release builds.
    - Run a full manual test on the release APK to ensure no Room or serialization issues due to obfuscation.

## 🎨 Assets & Identity

- [ ] **App Icon**: Verify adaptive icons are rendering correctly.
- [ ] **Dynamic Logo**: Check contrast of the header logo in both light and dark backgrounds.

## 📝 Content & Localization

- [ ] **Internationalization**: Ensure all new strings in `strings.xml` have equivalents in `values-en/strings.xml`.
- [ ] **Privacy Policy**: Ensure the app links to a valid Privacy Policy URL (Play Store requirement).

## 🧪 Quality & Testing

- [ ] **Regression Suite**: Run all unit and instrumented tests.
    - `./gradlew testDebugUnitTest connectedAndroidTest`
- [ ] **Physical Device Test**: Test `AlarmManager` behavior on a real device (ideally Samsung or Xiaomi to check power management issues).

---

## 📦 Generating the Bundle

Generate the Android App Bundle (AAB) for upload:
```bash
./gradlew clean app:bundleRelease
```
Artifact location: `app/build/outputs/bundle/release/app-release.aab`
