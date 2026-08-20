# 🧪 Testing Guide: Meus Remedinhos

> Comprehensive guide on the project's testing strategy, execution, and coverage analysis.

---

## 🚀 Quick Execution

### Unit Tests (JVM)
*Fast feedback (~15s), tests business logic and isolated components.*
```bash
./gradlew testDebugUnitTest
```

### Instrumented Tests (Android)
*End-to-end and UI validation (~5m), requires an emulator (API 34).*
```bash
./gradlew connectedAndroidTest
```

### Coverage Report
*Generates HTML report using JaCoCo.*
```bash
./gradlew testDebugUnitTest jacocoTestReport
# Report: app/build/reports/jacoco/jacocoTestReport/html/index.html
```

---

## 📊 Coverage Analysis

Current Overall Coverage: **78%** ✅ (Target: 85%)

### Coverage by Component

| Component | Target | Current | Status |
| :--- | :--- | :--- | :--- |
| **Alarm Scheduler** | 90% | 90% | ✅ Critical |
| **Notification Helper** | 85% | 85% | ✅ Critical |
| **ViewModel Logic** | 85% | 85% | ✅ Stable |
| **Data Repository** | 80% | 75% | ⚠️ Improvement Needed |
| **Room DAO** | 80% | 80% | ✅ Stable |
| **UI (Compose)** | 70% | 65% | ⚠️ Improvement Needed |

---

## 🔍 What is Tested?

### 1. Alarm & Background (Critical)
We verify that alarms are scheduled correctly, even for times that have already passed (rescheduling for the next day). We also test Android 12+ exact alarm permission fallbacks and Doze Mode bypasses.

### 2. Notification Dispatch
Tests ensure notification channels are created with correct importance, vibration patterns are active, and deep-links point to the correct event in the app.

### 3. Business Logic (ViewModel)
Complete coverage of event CRUD operations, medication management (add/remove/edit), and the complex clock emoji logic (14+ time-based cases).

### 4. Data Integrity
Room DAO tests validate that medication lists (stored as JSON) are correctly serialized/deserialized and that daily status resets don't lose user data.

---

## 🛠 Troubleshooting

### Emulator Issues
- **Offline:** Run `adb kill-server && adb start-server`.
- **Performance:** Use a Pixel 6 image with API 34 and ensure KVM is enabled on Linux.

### MockK Failures
Ensure `unmockkAll()` is called in the `@After` method of your tests to prevent state leakage between test cases.

### Timeout Errors
If Compose UI tests fail with timeouts, increase the `waitUntil` duration or verify the emulator isn't under heavy load.

---

## 📈 Roadmap

1. **Sprint 2:** Add tests for Widget rendering and complex error paths.
2. **Sprint 3:** Performance benchmarks for lists with 100+ items.
3. **Sprint 4:** Reach 85% overall code coverage.
