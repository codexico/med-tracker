# 👨‍💻 Developer Guide: Meus Remedinhos (Native)

> Complete reference for developers: setup, code structure, common patterns, testing, and debugging.

---

## 1. Local Development Setup

### 1.1 Prerequisites

```bash
# System Requirements
- OS: Linux, macOS, or Windows with WSL2
- JDK 17 (OpenJDK or Eclipse Adoptium)
- Android Studio: Ladybug (2024.1.1) or later
- Android SDK: API 34+ (Android 14)
- Gradle: 8.x (bundled with Android Studio)
```

### 1.2 Installation Steps

1. **Clone Repository**
   ```bash
   cd /path/to/med-tracker
   cd Kotlin
   ```

2. **Open in Android Studio**
   - File → Open → Select `Kotlin` folder
   - Android Studio downloads Gradle wrapper automatically

3. **Sync Gradle**
   - Android Studio prompts: "Sync Now" (top banner)
   - Wait for dependency download (2-5 minutes on first run)

4. **Verify Setup**
   ```bash
   ./gradlew tasks  # Should list available Gradle tasks
   ```

5. **Configure Emulator or Device**
   - **Emulator:** Android Studio → Device Manager → Create Virtual Device (API 34)
   - **Physical Device:** Enable USB debugging, connect via USB

### 1.3 Running the App

```bash
# Build and run on emulator/device
./gradlew clean assembleDebug && ./gradlew installDebug

# Or from Android Studio
# - Click "Run" button (green play icon)
# - Select emulator or device
# - Wait for build + installation (~2 min on first run)
```

### 1.4 Distrobox Environment (Linux)

If using Distrobox (Ubuntu 22.04 in container):

```bash
distrobox enter ubuntu22-android

cd /path/to/med-tracker

# Android Studio runs in container; emulator connects via KVM to host
./gradlew assembleDebug
```

See `DISTROBOX_GUIDE.md` for full setup.

---

## 2. Project Structure Deep Dive

### 2.1 Source Tree

```
app/src/main/java/com/franciscokahil/appMeusRemedinhos/
│
├── MainActivity.kt ......................... App entry point, DI setup
├── NavigationKeys.kt ....................... Navigation route constants
├── Navigation.kt ........................... NavHost configuration (backup)
│
├── ui/
│   ├── MainNavigation.kt ................... Primary NavHost (onboarding → dashboard)
│   │
│   ├── dashboard/
│   │   ├── DashboardScreen.kt ............. Main UI screen
│   │   ├── DashboardViewModel.kt .......... State management
│   │   ├── DashboardViewModelFactory.kt ... Dependency injection for ViewModel
│   │   ├── EventCard.kt ................... Individual event display (Composable)
│   │   └── AddEventDialog.kt .............. Add/edit event modal
│   │
│   ├── onboarding/
│   │   └── OnboardingScreen.kt ............ First-run setup flow
│   │
│   └── theme/
│       ├── Color.kt ....................... Color palette
│       ├── Type.kt ........................ Typography definitions
│       └── Theme.kt ....................... Material Design 3 composition
│
├── data/
│   ├── repository/
│   │   └── EventRepository.kt ............ Interface + impl (EventRepositoryImpl)
│   │
│   └── local/
│       ├── AppDatabase.kt ................ Room database definition
│       ├── EventDao.kt ................... Data Access Object (SQL queries)
│       ├── EventEntity.kt ................ Data model / entity
│       └── MedicationTypeConverter.kt .... Custom type for List<String>
│
├── background/
│   ├── AlarmScheduler.kt ................. Alarm scheduling interface + impl
│   ├── AlarmReceiver.kt .................. BroadcastReceiver for alarm triggers
│   └── NotificationHelper.kt ............. Notification dispatch logic
│
└── widget/
    ├── MedicationWidget.kt ............... GlanceAppWidget (home screen)
    ├── MedicationWidgetReceiver.kt ....... Widget provider declaration
    └── MedTrackerWidgetReceiver.kt ....... (Alternate name; may consolidate)

app/src/main/res/
├── values/strings.xml .................... UI strings (Portuguese)
├── values/colors.xml ..................... Color resources
├── values/dimens.xml ..................... Dimension constants
└── ...

app/src/test/java/com/franciscokahil/appMeusRemedinhos/
├── ExampleUnitTest.kt .................... Template unit test
├── ui/dashboard/DashboardViewModelTest.kt  ViewModel tests (JVM)
└── data/repository/EventRepositoryTest.kt  Repository tests

app/src/androidTest/java/com/franciscokahil/appMeusRemedinhos/
├── ExampleInstrumentedTest.kt ............ Template instrumented test
├── data/local/EventDaoTest.kt ............ DAO tests (emulator/device)
├── ui/dashboard/DashboardScreenTest.kt ... Compose UI tests
├── FullUserFlowTest.kt ................... End-to-end scenario test
└── DeepLinkTest.kt ....................... Navigation deep-link test
```

### 2.2 Module Dependencies

```
app/build.gradle.kts (Dependency versions)
  ↓ Applies plugins (Kotlin, KSP, Room)
  ↓ Includes versions from gradle/libs.versions.toml
  ↓ Generates annotation processors via KSP
  ↓ Produces app/build/outputs/apk/debug/*.apk
```

**Key Files:**
- `build.gradle.kts` — App module configuration
- `settings.gradle.kts` — Root module includes
- `gradle/libs.versions.toml` — Centralized dependency versions (Gradle 8.x best practice)
- `local.properties` — Local SDK path (auto-created, **do not commit**)

---

## 3. Common Development Patterns

### 3.1 Adding a New Feature (Example: Add Event)

**Step 1: Define Data Model**
```kotlin
// data/local/EventEntity.kt (already exists; extend if needed)
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val time: String,
    // ... other fields
)
```

**Step 2: Create/Update DAO**
```kotlin
// data/local/EventDao.kt
@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: EventEntity)
    
    // DAO method automatically generates SQL at compile-time
}
```

**Step 3: Update Repository Interface**
```kotlin
// data/repository/EventRepository.kt
interface EventRepository {
    suspend fun insertEvent(event: EventEntity)
    // ...
}
```

**Step 4: Implement in Repository**
```kotlin
// data/repository/EventRepositoryImpl.kt
class EventRepositoryImpl(...) : EventRepository {
    override suspend fun insertEvent(event: EventEntity) {
        eventDao.insertEvent(event)  // Suspending call
        updateWidgets()  // Side-effect: refresh widget
    }
}
```

**Step 5: Call from ViewModel**
```kotlin
// ui/dashboard/DashboardViewModel.kt
fun addEvent(label: String, time: String) {
    viewModelScope.launch {  // Coroutine scope
        val newEvent = EventEntity(
            id = UUID.randomUUID().toString(),
            title = label,
            time = time,
            isEnabled = true
        )
        repository.insertEvent(newEvent)
        scheduleEventAlarm(newEvent)  // Business logic
    }
}
```

**Step 6: Trigger from UI**
```kotlin
// ui/dashboard/DashboardScreen.kt
Button(onClick = { viewModel.addEvent("Café", "08:00") }) {
    Text("Add Event")
}
```

### 3.2 ViewModel Factory Pattern

Why? **Dependency Injection** without Hilt (simpler for small teams):

```kotlin
// ui/dashboard/DashboardViewModelFactory.kt
class DashboardViewModelFactory(
    private val repository: EventRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, alarmScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Usage in MainActivity.kt
val database = AppDatabase.getDatabase(this)
val eventDao = database.eventDao()
val repository = EventRepositoryImpl(this, eventDao)
val alarmScheduler = AlarmSchedulerImpl(this)
val factory = DashboardViewModelFactory(repository, alarmScheduler)

NavHost(...) {
    composable("dashboard") {
        DashboardScreen(
            viewModel = viewModel(factory = factory)
        )
    }
}
```

### 3.3 Pure Composable Components

**Why?** Testability, reusability, separation of concerns.

❌ **Anti-Pattern (tightly coupled):**
```kotlin
@Composable
fun EventCard() {
    val viewModel = viewModel<DashboardViewModel>()  // Direct access!
    val event = viewModel.events.collectAsState()
    
    Text(event.value.title)  // Hard to test
}
```

✅ **Correct Pattern (callback-driven):**
```kotlin
@Composable
fun EventCard(
    event: EventEntity,                    // Data parameter
    onCheckedChange: (Boolean) -> Unit,    // Callback
    onDelete: () -> Unit                   // Callback
) {
    Checkbox(
        checked = event.isTakenToday,
        onCheckedChange = onCheckedChange  // Delegate to parent
    )
    Text(event.title)  // Immutable; no side-effects
}

// Usage in parent
DashboardScreen(...) {
    EventCard(
        event = event,
        onCheckedChange = { viewModel.toggleEventStatus(event, it) }
    )
}
```

**Test easily:**
```kotlin
@Test
fun eventCardDisplaysTitle() {
    val event = EventEntity(
        id = "1", title = "Café", time = "08:00"
    )
    composeRule.setContent {
        EventCard(
            event = event,
            onCheckedChange = {},
            onDelete = {}
        )
    }
    composeRule.onNodeWithText("Café").assertIsDisplayed()
}
```

### 3.4 Handling Side Effects in ViewModel

**DO:** Use `launch { }` in `viewModelScope`
```kotlin
fun addEvent(label: String, time: String) {
    viewModelScope.launch {  // Structured concurrency
        repository.insertEvent(newEvent)
    }
    // Scope automatically cancelled when ViewModel cleared
}
```

**DON'T:** Use GlobalScope or bare Coroutine.launch
```kotlin
// ❌ Bad: May leak coroutines
GlobalScope.launch {
    repository.insertEvent(newEvent)
}
```

### 3.5 StateFlow Binding in Compose

```kotlin
// ViewModel
val events: StateFlow<List<EventEntity>> = repository.allEvents.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),  // Stop if no subscribers for 5s
    initialValue = emptyList()
)

// Screen
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val eventsList = viewModel.events.collectAsState()  // Compose binding
    
    Column {
        eventsList.value.forEach { event ->
            EventCard(event, ...)
        }
    }
    // Auto-recomposes when StateFlow emits
}
```

### 3.6 Room TypeConverter for Complex Types

**Problem:** Room doesn't natively support `List<String>`

**Solution:** Custom TypeConverter

```kotlin
// data/local/MedicationTypeConverter.kt
class MedicationTypeConverter {
    @TypeConverter
    fun fromMedicationList(medications: List<String>): String {
        return Json.encodeToString(medications)  // Kotlin serialization
    }

    @TypeConverter
    fun toMedicationList(json: String): List<String> {
        return if (json.isEmpty()) emptyList() else Json.decodeFromString(json)
    }
}

// Usage in Entity
@Entity
@TypeConverters(MedicationTypeConverter::class)
data class EventEntity(
    val medications: List<String> = emptyList()  // Stores as JSON string in SQLite
)
```

---

## 4. Testing Guide

### 4.1 Unit Tests (JVM, Fast, No Device)

**Location:** `app/src/test/java/`

**Run:**
```bash
./gradlew testDebugUnitTest
```

**Example: ViewModel Test**
```kotlin
@Test
fun addEventSchedulesAlarm() {
    // Arrange
    val mockRepository = mockk<EventRepository>()
    val mockAlarmScheduler = mockk<AlarmScheduler>()
    val viewModel = DashboardViewModel(mockRepository, mockAlarmScheduler)

    // Act
    viewModel.addEvent("Café", "08:00")
    // Note: Must wait for coroutine in real test (use runBlocking or TestDispatchers)

    // Assert
    verify { mockAlarmScheduler.scheduleAlarm(any(), any(), any(), 8, 0) }
}
```

**Coroutine Testing:**
```kotlin
@get:Rule
val instantExecutorRule = InstantTaskExecutorRule()

@Test
fun addEventUpdatesStateFlow() = runBlocking {
    // Use runBlocking to wait for suspend functions
    val mockRepository = mockk<EventRepository>()
    coEvery { mockRepository.insertEvent(any()) } just Runs
    
    val viewModel = DashboardViewModel(mockRepository, mockAlarmScheduler)
    viewModel.addEvent("Test", "08:00")
    
    // Verify via delayed collection
    delay(100)  // Allow coroutine time to execute
    // Assert state...
}
```

### 4.2 Instrumented Tests (Emulator/Device, Slow, Real Android)

**Location:** `app/src/androidTest/java/`

**Run:**
```bash
# Requires emulator running
./gradlew connectedAndroidTest
```

**Example: DAO Test**
```kotlin
@RunWith(AndroidJUnit4::class)
class EventDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: EventDao

    @Before
    fun setUp() {
        // In-memory database for testing (no files, auto-cleanup)
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries()  // Allow queries on main thread (OK for tests)
         .build()
        dao = db.eventDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveEvent() = runBlocking {
        // Arrange
        val event = EventEntity(
            id = "1",
            title = "Café",
            time = "08:00"
        )

        // Act
        dao.insertEvent(event)
        val retrieved = dao.getAllEvents().first()  // Get first emission

        // Assert
        assertThat(retrieved).hasSize(1)
        assertThat(retrieved[0].title).isEqualTo("Café")
    }
}
```

**Example: Compose UI Test**
```kotlin
@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dashboardDisplaysEventCard() {
        // Arrange
        val event = EventEntity(
            id = "1",
            title = "Café da Manhã",
            time = "08:00",
            medications = listOf("Vitamina D"),
            icon = "🕐"
        )

        // Act
        composeRule.setContent {
            EventCard(
                event = event,
                onCheckedChange = {},
                onDelete = {}
            )
        }

        // Assert
        composeRule.onNodeWithText("Café da Manhã").assertIsDisplayed()
        composeRule.onNodeWithText("08:00").assertIsDisplayed()
        composeRule.onNodeWithText("Vitamina D").assertIsDisplayed()
    }
}
```

**Example: Full User Flow (End-to-End)**
```kotlin
@RunWith(AndroidJUnit4::class)
class FullUserFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun userCanAddAndMarkEventComplete() {
        // Arrange
        composeRule.setContent {
            MeusRemedinhoApp()  // Full app
        }

        // Act 1: Add event
        composeRule.onNodeWithContentDescription("Add").performClick()
        composeRule.onNodeWithPlaceholder("Event name").performTextInput("Café")
        composeRule.onNodeWithContentDescription("Select time").performClick()
        // ... time picker interactions
        composeRule.onNodeWithText("Create").performClick()

        // Assert 1: Event appears in dashboard
        composeRule.onNodeWithText("Café").assertIsDisplayed()

        // Act 2: Mark complete
        composeRule.onNodeWithTag("event_checkbox_café").performClick()

        // Assert 2: Visual indicator shows completion
        composeRule.onNodeWithTag("event_café").assertHasClickAction()
    }
}
```

### 4.3 Testing Checklist

Before submitting PR:

- [ ] Unit tests pass: `./gradlew testDebugUnitTest`
- [ ] Instrumented tests pass: `./gradlew connectedAndroidTest`
- [ ] No lint warnings: `./gradlew lint`
- [ ] Code coverage >80% for modified classes (use IDE's coverage tool)
- [ ] Manual testing on API 34 emulator

---

## 5. Debugging

### 5.1 Android Studio Debugger

1. **Set Breakpoint:** Click left gutter next to line number
2. **Run in Debug Mode:** Run → Debug (or Shift+F9)
3. **Step Through:** Step Over (F10), Step Into (F11)
4. **Inspect Variables:** Hover or expand in Variables panel

### 5.2 Logcat Filtering

```bash
# View logs in real-time
adb logcat | grep "appMeusRemedinhos"

# Or in Android Studio: Logcat tab → Filter by app name
```

### 5.3 Database Inspection

```bash
# Pull database from device
adb pull /data/data/com.franciscokahil.appMeusRemedinhos/databases/meus_remedinhos.db

# Open with SQLite browser
sqlite3 meus_remedinhos.db
sqlite> SELECT * FROM events;
```

### 5.4 Notification Testing

```bash
# Trigger alarm manually (emulator)
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED

# Set time forward (emulator)
adb shell date 061515002026  # June 15, 15:00, 2026
```

### 5.5 Widget Testing

```bash
# Force widget update
adb shell am broadcast -a com.franciscokahil.appMeusRemedinhos.WIDGET_UPDATE

# Or restart app (triggers repository update → widget refresh)
adb shell am force-stop com.franciscokahil.appMeusRemedinhos
# Then re-open app
```

---

## 6. Code Style & Best Practices

### 6.1 Kotlin Style Guide

- **Naming:** camelCase for functions/variables, PascalCase for classes
  ```kotlin
  fun addEvent(...) { }               // ✅
  fun AddEvent(...) { }               // ❌
  
  class EventCard { }                 // ✅
  class eventCard { }                 // ❌
  ```

- **Immutability:** Use `val`, avoid `var`
  ```kotlin
  val event = EventEntity(...)        // ✅ Prefer
  var event = EventEntity(...)        // ❌ Avoid if possible
  ```

- **Null Safety:** Use nullability explicitly
  ```kotlin
  val event: EventEntity? = null      // ✅ Explicit nullable
  val event = EventEntity()           // ✅ Non-null by default
  val event: EventEntity = null       // ❌ Compiler error (good!)
  ```

- **Scopes:** Use `apply`, `let`, `run` appropriately
  ```kotlin
  Calendar.getInstance().apply {      // ✅ Modify state, return receiver
      set(Calendar.HOUR_OF_DAY, 8)
      set(Calendar.MINUTE, 0)
  }
  
  event?.let {                         // ✅ Do work if non-null
      viewModel.deleteEvent(it)
  }
  ```

### 6.2 Compose Best Practices

- **State Hoisting:** Pass state upward
  ```kotlin
  // ❌ Don't: State in child
  @Composable
  fun EventCard() {
      var isExpanded by remember { mutableStateOf(false) }
  }
  
  // ✅ Do: State in parent
  @Composable
  fun DashboardScreen() {
      var isExpanded by remember { mutableStateOf(false) }
      EventCard(isExpanded = isExpanded, onExpandChange = { ... })
  }
  ```

- **Avoid Recomposition:** Use `.key()` for stable items
  ```kotlin
  LazyColumn {
      items(
          events,
          key = { event -> event.id }  // ✅ Stable key
      ) { event ->
          EventCard(event)
      }
  }
  ```

- **Modifiers:** Chain in consistent order
  ```kotlin
  Box(
      modifier = Modifier
          .size(100.dp)
          .background(Color.Blue)
          .padding(8.dp)
          .clickable { }
  )
  ```

### 6.3 Room Best Practices

- **Always return Flow for queries**
  ```kotlin
  @Query("SELECT * FROM events")
  fun getAllEvents(): Flow<List<EventEntity>>  // ✅ Reactive
  
  @Query("SELECT * FROM events")
  fun getAllEventsSync(): List<EventEntity>    // ❌ Avoid
  ```

- **Use suspend for writes**
  ```kotlin
  @Insert
  suspend fun insertEvent(event: EventEntity)  // ✅
  
  @Insert
  fun insertEventSync(event: EventEntity)      // ❌ Blocks UI
  ```

- **Leverage TypeConverters for complex types**
  ```kotlin
  data class EventEntity(
      val medications: List<String> = emptyList()  // ✅ TypeConverter handles it
  )
  ```

### 6.4 Naming Conventions

| Entity | Convention | Example |
|--------|-----------|---------|
| **Composable Function** | PascalCase, descriptive | `EventCard`, `AddEventDialog` |
| **Regular Function** | camelCase | `scheduleEventAlarm`, `getClockEmoji` |
| **Constant** | UPPER_SNAKE_CASE | `CHANNEL_ID`, `TAG_DEBUG` |
| **Variable** | camelCase | `eventsList`, `isExpanded` |
| **Class** | PascalCase | `EventEntity`, `DashboardViewModel` |
| **Package** | lowercase.reversed.domain | `com.franciscokahil.appMeusRemedinhos.ui` |
| **String Key** | snake_case, prefixed | `event_title_label`, `button_add` |

### 6.5 Documentation

```kotlin
/**
 * Schedules a daily alarm for medication reminder.
 *
 * @param event The event containing time and medication details
 * @throws IllegalArgumentException If event.time is malformed (not "HH:MM")
 *
 * Example:
 * ```kotlin
 * val event = EventEntity(id = "1", title = "Café", time = "08:00")
 * viewModel.scheduleEventAlarm(event)
 * ```
 */
fun scheduleEventAlarm(event: EventEntity) {
    // Implementation...
}
```

---

## 7. Build & Deployment

### 7.1 Build Variants

```bash
# Debug build (for development)
./gradlew assembleDebug

# Release build (for distribution)
./gradlew assembleRelease
```

### 7.2 Gradle Tasks

```bash
# Common tasks
./gradlew clean                  # Delete build artifacts
./gradlew build                  # Full build
./gradlew testDebugUnitTest      # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests
./gradlew lint                   # Static analysis
./gradlew bundleRelease          # Android App Bundle (for Google Play)
```

### 7.3 Dependency Updates

```bash
# Check for updates
./gradlew dependencyUpdates

# Update specific dependency
# Edit gradle/libs.versions.toml and run
./gradlew clean build
```

### 7.4 Publishing to Google Play Store

1. **Generate Keystore** (one-time)
   ```bash
   keytool -genkey -v -keystore release.keystore \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias meus-remedinhos
   ```

2. **Sign Bundle**
   ```bash
   cp release.keystore app/
   # Update build.gradle.kts with keystore signing config
   ./gradlew bundleRelease
   ```

3. **Upload to Play Console**
   - Go to Google Play Console
   - Upload `app/build/outputs/bundle/release/app-release.aab`
   - Fill in store listing, screenshots, etc.
   - Submit for review

---

## 8. Troubleshooting

### Issue: Gradle sync fails

**Solution:**
```bash
./gradlew clean
# Remove ~/.gradle/caches if persistent
rm -rf ~/.gradle/caches
./gradlew sync
```

### Issue: Emulator is slow

**Solution:** Enable KVM acceleration
```bash
# Linux
sudo apt install qemu-kvm libvirt-bin
# Check: cat /proc/cpuinfo | grep vmx  # Intel or AMD flag
```

### Issue: Notification not firing

**Solution:**
- Check permission: `adb shell pm check-permission android.permission.SCHEDULE_EXACT_ALARM com.franciscokahil.appMeusRemedinhos`
- Check alarm scheduled: `adb shell dumpsys alarm | grep appMeusRemedinhos`
- Verify time is correct: `adb shell date`

### Issue: Widget not updating

**Solution:**
- Force widget update: `adb shell am broadcast -a android.appwidget.action.APPWIDGET_UPDATE`
- Check Glance logs: `adb logcat | grep Glance`

---

## 9. Git Workflow & PRs

### 9.1 Feature Branch

```bash
# Create feature branch
git checkout -b feature/medication-history

# Make changes, commit
git add -A
git commit -m "feat: add medication history view"

# Push and create PR
git push origin feature/medication-history
```

### 9.2 PR Checklist

- [ ] Tests added/updated
- [ ] No breaking changes
- [ ] Code follows style guide
- [ ] Documentation updated
- [ ] Squashed commits (clean history)

### 9.3 Commit Message Format

```
feat: add medication history view
  - Display past 30 days of medication completions
  - Implement HistoryScreen composable
  - Add HistoryRepository for data fetching

fix: correct alarm scheduling edge case on Android 12+

docs: update architecture diagram in ARCHITECTURE.md

refactor: consolidate widget receiver classes
```

---

## 10. IDE Tips & Shortcuts

| Action | Shortcut | Note |
|--------|----------|------|
| Run app | Shift+F10 | Green play icon |
| Debug app | Shift+F9 | Add breakpoints first |
| Format code | Ctrl+Alt+L | Applies style rules |
| Optimize imports | Ctrl+Alt+O | Remove unused imports |
| Rename | Shift+F6 | Safe refactoring |
| Extract variable | Ctrl+Alt+V | Create `val` from selection |
| Extract function | Ctrl+Alt+M | Create function from code block |
| Quick fix (lightbulb) | Alt+Enter | Code suggestions |
| Find usages | Alt+F7 | Where is this used? |
| Go to implementation | Ctrl+Alt+B | Jump to impl (interface → class) |

---

## 11. Continuous Integration (Future)

When adding CI/CD (GitHub Actions, GitLab CI):

```yaml
# Example: GitHub Actions workflow
name: Build & Test
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: 17
      - run: ./gradlew build
      - run: ./gradlew lint
      - run: ./gradlew testDebugUnitTest
```

---

## 12. Additional Resources

- [Android Developer Docs](https://developer.android.com/)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [Room & Persistence](https://developer.android.com/training/data-storage/room)
- [AlarmManager Guide](https://developer.android.com/training/scheduling/alarms)
- [Jetpack Glance Widgets](https://developer.android.com/develop/ui/compose/glance)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)


