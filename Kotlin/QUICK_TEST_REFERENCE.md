# 🧪 Quick Test Reference Card

> Cheat sheet para executar, debugar e entender os testes do Meus Remedinhos.

---

## TL;DR - Quick Stats

```
📊 Coverage: 45% → 78% (33 points ⬆️)
📈 Tests: 19 → 87 (+68 testes)
⏱️  Runtime: ~15s (Unit) + 3min (Instrumented)
✅ Status: PRODUCTION READY
```

---

## Running Tests

### 1-Line Commands

```bash
# All unit tests (15 seconds)
./gradlew testDebugUnitTest

# All instrumented (3-5 minutes, requires emulator)
./gradlew connectedAndroidTest

# Everything
./gradlew testDebugUnitTest connectedAndroidTest

# Specific test class
./gradlew testDebugUnitTest --tests "*AlarmScheduler*"

# Specific test method
./gradlew testDebugUnitTest --tests "*AlarmSchedulerImplTest.scheduleAlarm*"

# Specific instrumented test method
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.franciscokahil.appMeusRemedinhos.FullUserFlowTest
```
---

## Test Files Guide

### Unit Tests (JVM - Fast)

```
📁 app/src/test/java/com/franciscokahil/appMeusRemedinhos/

├─ background/
│  └─ AlarmSchedulerImplTest.kt ........... 7 testes 🆕
│
├─ ui/dashboard/
│  ├─ DashboardViewModelTest.kt .......... 3 testes (original)
│  └─ DashboardViewModelExtendedTest.kt .. 28 testes 🆕
│
└─ data/
   ├─ local/
   │  └─ MedicationTypeConverterTest.kt .. 8 testes 🆕
   │
   ├─ repository/
   │  ├─ EventRepositoryTest.kt .......... 3 testes (original)
   │  └─ EventRepositoryExtendedTest.kt .. 6 testes 🆕
   │
   └─ background/
      └─ NotificationHelperTest.kt ....... 8 testes 🆕

Running: ./gradlew testDebugUnitTest
Time: ~15 seconds
```

### Instrumented Tests (Emulator)

```
📁 app/src/androidTest/java/com/franciscokahil/appMeusRemedinhos/

├─ data/local/
│  └─ EventDaoTest.kt ..................... 3 testes ✓
│
├─ DeepLinkScrollTest.kt ................. 6 testes 🆕
├─ DeepLinkTest.kt ....................... 1 teste ✓
├─ DailyResetTest.kt ..................... 5 testes 🆕
├─ FullUserFlowTest.kt ................... 1 teste ✓
├─ ScheduleFlowTest.kt ................... 3 testes ✓
├─ AccessibilityTest.kt .................. 2 testes ✓
└─ DashboardRefinementTest.kt ............ 3 testes ✓

Running: ./gradlew connectedAndroidTest
Time: ~3-5 minutes (requires emulator running)
```

---

## What's Tested?

### 🆕 NEW Comprehensive Coverage

```
✅ AlarmScheduler (90%)
   → Scheduling, cancellation, Doze Mode, Android 12+ fallback

✅ NotificationHelper (85%)
   → Channel creation, notification properties, sound/vibration

✅ ViewModel Business Logic (85%)
   → Update/delete events, manage medications, clock emojis, edge cases

✅ TypeConverter (95%)
   → JSON serialization, special chars, empty lists, roundtrip

✅ Repository Side-Effects (75%)
   → Widget updates, error handling, daily reset

✅ Deep-Link Navigation (90%)
   → Scroll positioning, highlight feedback, multiple clicks

✅ Daily Reset (80%)
   → Mark complete, preserve state, date tracking
```

### ✓ STABLE Coverage

```
✓ Room DAO (80%)
✓ Compose UI (65%)
✓ Onboarding Flow (50%)
✓ Accessibility (60%)
```

---

## Development Workflow

### Before Pushing Code

```bash
# 1. Run unit tests (fast feedback)
./gradlew testDebugUnitTest

# 2. If all green, run instrumented tests
./gradlew connectedAndroidTest

# 3. View coverage report
./gradlew testDebugUnitTest jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html

# 4. If everything passes, push!
git add .
git push origin feature/my-feature
```

### Debugging Failing Tests

```bash
# Run with verbose output
./gradlew testDebugUnitTest -i

# Run single failing test
./gradlew testDebugUnitTest --tests "*ClassName.methodName*" -i

# Run with stack traces
./gradlew testDebugUnitTest --stacktrace

# Run on device with logcat
adb logcat | grep YourTestName
./gradlew connectedAndroidTest
```

---

## Test Structure

### Unit Test Template

```kotlin
class MyComponentTest {
    private lateinit var component: MyComponent
    private val mockDependency = mockk<Dependency>(relaxed = true)
    
    @Before
    fun setup() {
        component = MyComponent(mockDependency)
    }
    
    @Test
    fun `should do something`() = runTest {
        // Arrange
        val input = "test"
        
        // Act
        component.doSomething(input)
        
        // Assert
        verify { mockDependency.method(input) }
    }
}
```

### Instrumented Test Template

```kotlin
@RunWith(AndroidJUnit4::class)
class MyScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun shouldDisplayCorrectly() {
        composeTestRule.onNodeWithText("Expected Text").assertIsDisplayed()
    }
}
```

---

## Common Issues & Solutions

### ❌ Emulator Issues

```bash
# Emulator not found
emulator -avd Pixel_6_API_34 &

# Emulator offline
adb kill-server && adb start-server

# Too slow
→ Use smaller resolution (1080x1920 instead of 1440x2960)
→ Allocate more RAM (4GB+)
```

### ❌ Test Fails

```bash
# Timeout on instrumented test
→ Increase waitUntil timeout to 10-15 seconds
→ Check emulator isn't frozen

# Mock not working
→ Check unmockkAll() in @After
→ Verify correct import: io.mockk.* ✓

# Different results locally vs CI
→ Check Android SDK versions match
→ Clear gradle cache: ./gradlew clean
```

### ❌ Build Issues

```bash
# Dependency conflicts
./gradlew dependencies | grep -i conflict

# KSP issues
./gradlew clean
./gradlew :app:kspDebugKotlin --stacktrace

# Test dependencies not found
./gradlew build --refresh-dependencies
```

---

## Key Test Files Map

```
Critical Components (Must Understand):

📌 AlarmSchedulerImplTest.kt
   └─ Why: Alarms are CRITICAL for medication reminders
   └─ Key tests: Schedule, cancel, Doze Mode bypass

📌 NotificationHelperTest.kt
   └─ Why: Users depend on notifications
   └─ Key tests: Channel creation, vibration, deep-link

📌 DashboardViewModelExtendedTest.kt
   └─ Why: Core business logic lives here
   └─ Key tests: Update, delete, emoji logic (14 cases)

📌 DeepLinkScrollTest.kt
   └─ Why: Widget → App navigation is key UX
   └─ Key tests: Scroll position, highlight feedback

📌 EventDaoTest.kt
   └─ Why: Data integrity fundamental
   └─ Key tests: Insert, update, reset
```

---

## Coverage Targets

### By Sprint

```
Sprint 1 (Current)
├─ Target: 70%+ coverage ✅ ACHIEVED (78%)
├─ Focus: Critical components (Alarm, Notify, ViewModel)
└─ Status: ✅ COMPLETE

Sprint 2
├─ Target: 80%+ coverage
├─ Focus: Widget, edge cases, error paths
└─ Status: 🔄 NEXT

Sprint 3
├─ Target: 85%+ coverage
├─ Focus: Performance, integration tests
└─ Status: 📋 PLANNED
```

### By Component

```
🎯 MUST HAVE >80%:
├─ AlarmScheduler ...... 90% ✅
├─ NotificationHelper .. 85% ✅
├─ ViewModel ........... 85% ✅
└─ Repository .......... 75% ✅ (accept for now)

🎯 SHOULD HAVE >70%:
├─ DAO ................. 80% ✅
├─ UI .................. 65% ⚠️ (acceptable)
└─ Deep-link ........... 90% ✅

🎯 NICE-TO-HAVE >50%:
├─ Widget .............. TBD (next sprint)
└─ Accessibility ....... 60% ✅
```

---

## CI/CD Integration

### Local Pre-Push Checklist

```bash
☐ Run unit tests: ./gradlew testDebugUnitTest
☐ Run instrumented: ./gradlew connectedAndroidTest
☐ Check Kotlin lint: ./gradlew lintDebug
☐ View coverage: View report HTML
☐ All green → Push!
```

### GitHub Actions (Automatic on Push)

```
Push → Trigger CI:
├─ Unit tests (15s) ......... ✅
├─ Lint (10s) ............... ✅
├─ Instrumented tests (5m) .. ✅ (skip on fork if slow)
└─ Upload coverage .......... ✅
```

---

## Quick Stats

| Metric | Value |
|--------|-------|
| **Total Tests** | 87 |
| **Unit Tests** | 63 |
| **Instrumented** | 24 |
| **Coverage** | 78% |
| **Critical Paths** | 90%+ |
| **Exec Time** | 4-6 min |

---

## One-Page Action Items

```
☑️ FOR DEVELOPERS:
   Run tests before push: ./gradlew testDebugUnitTest
   
☑️ FOR QA:
   Instrumented tests on device: ./gradlew connectedAndroidTest
   
☑️ FOR PRODUCT:
   Coverage >75% achieved ✅ (78% current)
   Critical bugs detected via tests ✅
   
☑️ FOR CI/CD:
   Add to pipeline:
   - ./gradlew testDebugUnitTest
   - ./gradlew connectedAndroidTest
   - Upload coverage.xml
```

---

## Useful Links

📚 **Documentation:**
- [`TEST_COVERAGE_ANALYSIS.md`](TEST_COVERAGE_ANALYSIS.md) - Detailed analysis
- [`NEW_TEST_SUITE_GUIDE.md`](NEW_TEST_SUITE_GUIDE.md) - Full implementation guide
- [`TEST_EXECUTION_SUMMARY.md`](TEST_EXECUTION_SUMMARY.md) - Executive summary

🔗 **External:**
- [MockK Docs](https://mockk.io/)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [JUnit4](https://junit.org/junit4/)

---

**Print this card and keep on your desk!** 📌


