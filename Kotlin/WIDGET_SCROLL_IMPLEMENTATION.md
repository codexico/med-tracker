# 🔗 Widget Deep-Linking & Auto-Scroll Implementation

> Technical documentation on how clicking a widget event scrolls the app to display the corresponding card.

---

## Overview

When a user clicks on an event in the widget (home screen), the following happens:

```
┌────────────────────────────────────────────────────┐
│ User: Tap "Antes de dormir" in widget              │
└────────────┬─────────────────────────────────────────┘
             ↓
┌────────────────────────────────────────────────────┐
│ Widget: Generate deep-link with event ID           │
│ meusremedinhos://event/{eventId}                  │
└────────────┬─────────────────────────────────────────┘
             ↓
┌────────────────────────────────────────────────────┐
│ Android OS: Launch MainActivity with deep-link     │
│ Intent { action=VIEW, data=meusremedinhos://... }  │
└────────────┬─────────────────────────────────────────┘
             ↓
┌────────────────────────────────────────────────────┐
│ MainActivity.handleIntent(): Extract ID            │
│ highlightedEventId.value = eventId                │
└────────────┬─────────────────────────────────────────┘
             ↓
┌────────────────────────────────────────────────────┐
│ Compose: Re-compose MainNavigation with            │
│ highlightedId = eventId                           │
└────────────┬─────────────────────────────────────────┘
             ↓
┌────────────────────────────────────────────────────┐
│ DashboardScreen.LaunchedEffect: Detect            │
│ highlightedId != null && events loaded             │
└────────────┬─────────────────────────────────────────┘
             ↓
┌────────────────────────────────────────────────────┐
│ 1. Find event index in list                        │
│ 2. Wait 200ms for LazyColumn rendering             │
│ 3. animateScrollToItem(index)                      │
│ 4. Highlight background color (2sec)              │
│ 5. Call onHighlightedConsumed()                    │
└────────────────────────────────────────────────────┘
             ↓
┌────────────────────────────────────────────────────┐
│ User sees: Card scrolled into viewport,            │
│ highlighted with shimmer effect                    │
└────────────────────────────────────────────────────┘
```

---

## Code Flow

### Step 1: Widget Creates Deep-Link Intent

**File:** `widget/MedicationWidget.kt` (lines 89-95)

```kotlin
@Composable
private fun WidgetEventItem(context: Context, event: EventEntity) {
    // Construct URI: meusremedinhos://event/{eventId}
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("meusremedinhos://event/${event.id}")).apply {
        setClass(context, MainActivity::class.java)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val action = actionStartActivity(intent)
    
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            ...
            .clickable(action)  // Tap to trigger intent
    ) {
        // Widget event item UI
    }
}
```

**Key Points:**
- URI scheme: `meusremedinhos://` (custom scheme for app)
- URI host: `event`
- URI path: `/{eventId}` (e.g., `/a1b2c3d4-e5f6-7890`)
- Intent flags: `FLAG_ACTIVITY_NEW_TASK` (for widget context)

---

### Step 2: MainActivity Intercepts Deep-Link

**File:** `MainActivity.kt` (lines 30-36, 80-92)

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ...
    handleIntent(intent)  // Process initial intent
    // ...
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIntent(intent)  // Process subsequent intents (app already running)
}

private fun handleIntent(intent: Intent?) {
    intent?.data?.let { uri ->
        if (uri.scheme == "meusremedinhos" && uri.host == "event") {
            val eventId = uri.lastPathSegment
            if (!eventId.isNullOrEmpty()) {
                highlightedEventId.value = eventId
                Log.d("MainActivity", "Deep-link received for event: $eventId")
            }
        }
    }
}
```

**Key Points:**
- `onCreate()`: Handles cold start (app not running)
- `onNewIntent()`: Handles warm start (app already running, widget click)
- URI parsing: Extract last path segment as eventId
- State management: Set `highlightedEventId` (triggers recomposition via StateFlow)

---

### Step 3: Compose Receives Highlighted ID

**File:** `ui/MainNavigation.kt` (lines 33-37)

```kotlin
composable("dashboard") {
    DashboardScreen(
        highlightedId = highlightedId,          // Received from MainActivity
        onHighlightedConsumed = onHighlightedConsumed  // Callback to clear state
    )
}
```

State flows: `MainActivity.highlightedEventId` → `MainNavigation.highlightedId` → `DashboardScreen.highlightedId`

---

### Step 4: DashboardScreen Performs Scroll & Highlight

**File:** `ui/dashboard/DashboardScreen.kt` (lines 53-76)

```kotlin
val listState = rememberLazyListState()
var activeHighlightId by remember { mutableStateOf<String?>(null) }

// Scroll to highlighted item from widget deep-link
LaunchedEffect(highlightedId, events) {
    if (highlightedId != null && events.isNotEmpty()) {
        val index = events.indexOfFirst { it.id == highlightedId }
        if (index != -1) {
            // 1. Wait for LazyColumn to be laid out
            delay(200)
            
            // 2. Animate scroll to item
            listState.animateScrollToItem(index = index, scrollOffset = 0)
            
            // 3. Trigger visual highlight
            activeHighlightId = highlightedId
            
            // 4. Keep highlight visible for 2 seconds
            delay(2000)
            
            // 5. Clear and signal consumed
            activeHighlightId = null
            onHighlightedConsumed()
        }
    }
}
```

**Timing Breakdown:**
- T=0ms: LaunchedEffect triggered
- T=200ms: LazyColumn layout complete
- T=200ms: Start scroll animation (~300ms)
- T=500ms: Scroll complete
- T=500ms-T=2500ms: Highlight background visible
- T=2500ms: Clear highlight & call `onHighlightedConsumed()`

**LaunchedEffect Dependencies:**
- `highlightedId`: Triggers whenever highlighted ID changes
- `events`: Triggers whenever event list changes (ensures items are available)

---

### Step 5: Visual Highlight Effect

**File:** `ui/dashboard/DashboardScreen.kt` (lines 146-173)

```kotlin
items(events, key = { it.id }) { event ->
    val isHighlighted = activeHighlightId == event.id
    
    val highlightColor by animateColorAsState(
        targetValue = if (isHighlighted) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) 
        else 
            Color.Transparent,
        animationSpec = tween(500)  // Smooth color animation
    )

    if (expandedEventId == null || isExpanded) {
        EventCard(
            event = event,
            ...
            highlightColor = highlightColor  // Applied to background
        )
    }
}
```

**Visual Feedback:**
- Highlight color: Primary color with 25% alpha (semi-transparent overlay)
- Animation: 500ms smooth fade-in/out
- Duration: Visible for 2 seconds total

---

## Important Implementation Details

### 1. Delay Before Scroll (200ms)

```kotlin
delay(200)
listState.animateScrollToItem(index = index, scrollOffset = 0)
```

**Why?**
- LazyColumn needs time to measure and lay out items
- Without delay, scroll may not work or scroll to wrong position
- 200ms is safe margin (even with slow devices)

### 2. Order of Operations

```
✅ CORRECT:
    1. Find index
    2. Delay for layout
    3. Scroll
    4. Highlight visual
    5. Delay 2 sec
    6. Call onHighlightedConsumed()

❌ WRONG:
    1. Find index
    2. Call onHighlightedConsumed() <- zeors highlightedId too early!
    3. Delay
    4. Scroll (but highlightedId is now null)
```

Calling `onHighlightedConsumed()` **too early** causes premature recomposition and scroll failure.

### 3. Key in items() For Stability

```kotlin
items(events, key = { it.id }) { event ->
    ...
}
```

**Why?**
- Stable key ensures Compose re-uses the same slot for same event
- Without key, item positions can shift during recomposition
- Prevents scroll jumping/flickering

### 4. scrollOffset = 0 (Top Alignment)

```kotlin
listState.animateScrollToItem(index = index, scrollOffset = 0)
```

- `scrollOffset = 0`: Aligns item to top of viewport
- `scrollOffset > 0`: Can be used for padding below item

---

## Deep-Link URI Format

### Valid URIs

```
meusremedinhos://event/123e4567-e89b-12d3-a456-426614174000   ✅
meusremedinhos://event/abc                                     ✅
meusremedinhos://event/                                        ❌ Empty path
meusremedinhos://other/123                                     ❌ Wrong host
https://meusremedinhos/event/123                               ❌ Wrong scheme
```

### URI Parsing (AndroidManifest.xml)

For explicit deep-link support (future enhancement), add to AndroidManifest.xml:

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="meusremedinhos"
            android:host="event" />
    </intent-filter>
</activity>
```

---

## Debugging

### Enable Logcat Filtering

```bash
adb logcat | grep "MainActivity"
```

**Expected logs:**
```
D/MainActivity: Deep-link received for event: 123e4567-e89b-12d3-a456-426614174000
```

### Test Deep-Link Manually

```bash
# From emulator/device terminal
adb shell am start -a android.intent.action.VIEW \
  -d "meusremedinhos://event/test-event-id" \
  com.franciscokahil.appMeusRemedinhos
```

### Verify Scroll Position

1. Open app
2. Widget click on bottom event ("Antes de dormir")
3. App opens → should scroll to bottom, highlight visible
4. Check Logcat for "Deep-link received"

---

## Edge Cases & Fixes

### Edge Case 1: Widget Click While App Running

**Scenario:** App open, user clicks widget event (not current screen)

**Expected:** Scroll to event on current screen

**Implementation:** `onNewIntent()` handles this case

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIntent(intent)  // Re-process new intent
}
```

### Edge Case 2: Event Deleted Before App Opens

**Scenario:** User creates event, closes app, deletes event via settings, reopens widget intent

**Expected:** App opens, no crash, no infinite scroll attempt

**Implementation:** Index check prevents crash

```kotlin
val index = events.indexOfFirst { it.id == highlightedId }
if (index != -1) {  // Only scroll if found
    listState.animateScrollToItem(index = index, scrollOffset = 0)
}
```

### Edge Case 3: Rapid Widget Clicks

**Scenario:** User rapidly clicks multiple events in widget

**Expected:** Each click should trigger new scroll (last click wins)

**Implementation:** LaunchedEffect dependencies handle this

```kotlin
LaunchedEffect(highlightedId, events) {  // Re-triggers if highlightedId changes
    // Scroll logic
}
```

---

## Testing

### Unit Test Example (Future)

```kotlin
@Test
fun testDeepLinkExtraction() {
    val uri = Uri.parse("meusremedinhos://event/test-id-123")
    val eventId = uri.lastPathSegment
    assert(eventId == "test-id-123")
}
```

### Integration Test Example (Future)

```kotlin
@RunWith(AndroidJUnit4::class)
class WidgetScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun widgetClickScrollsToEvent() {
        // 1. Open app
        // 2. Create events (including "Antes de dormir")
        // 3. Simulate widget intent with last event ID
        // 4. Verify scroll position and highlight visible
    }
}
```

---

## Future Enhancements

### 1. Scroll with Padding

```kotlin
listState.animateScrollToItem(index = index, scrollOffset = 100.dp.roundToPx())
```
Position event 100dp from top (adds padding).

### 2. Expand EventCard on Scroll

```kotlin
LaunchedEffect(...) {
    ...
    expandedEventId = highlightedId  // Auto-expand for better visibility
}
```

### 3. Animated Bounce Effect

Add bounce animation in addition to scroll (more eye-catching).

### 4. Browser Support (Future)

If offering web companion app:
```kotlin
// Make app openable from web with deep-link
// https://play.google.com/store/apps/details?id=com.franciscokahil.appMeusRemedinhos&link=meusremedinhos://event/123
```

---

## References

- `widget/MedicationWidget.kt` - Widget deep-link creation
- `MainActivity.kt` - Intent handling
- `ui/MainNavigation.kt` - Navigation flow
- `ui/dashboard/DashboardScreen.kt` - Scroll implementation
- [Android Deep-Linking Guide](https://developer.android.com/training/app-links/deep-linking)
- [Compose LazyColumn Scroll](https://developer.android.com/reference/androidx/compose/foundation/lazy/LazyListState)


