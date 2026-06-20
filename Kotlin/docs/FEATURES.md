# 🎯 Features & Capabilities: Meus Remedinhos

> Complete feature inventory with user stories, acceptance criteria, and current implementation status.

---

## 1. Onboarding (Empty State Experience)

### Description
UX 2.0 starts users with a clean dashboard, using interactive tooltips and an empty state message to guide their first actions.

### User Story
> As a new user, I want a clear starting point with helpful guidance so I can manually create a schedule that fits my specific needs.

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **Empty State** | Display friendly message and illustration when no events exist | ✅ Implemented |
| **Pulsating Tooltips** | Guide user toward the FAB and preset menu with animations | ✅ Implemented |
| **Manual Presets** | Offer quick-add options for common times (Acordar, Café, etc.) via FAB menu | ✅ Implemented |
| **Permission Just-in-Time** | Request notification permissions only when the first event is created | ✅ Implemented |
| **Dynamic Guidance** | Tooltips automatically disappear once the first event is added | ✅ Implemented |

### Technical Implementation
- **Location:** `ui/dashboard/DashboardScreen.kt` (OnboardingEmptyState)
- **Tooltip Logic:** `rememberTooltipState` with `LaunchedEffect` monitoring event count.
- **Permission Flow:** Handled in `DashboardScreen` confirm action via `ActivityResultLauncher`.

---

## 2. Dashboard (Main Screen)

### Description
The primary screen where users view today's medication schedule and manage event completion status.

### User Story
> As a user, I want to see all my medications for today organized by time, with visual indicators of what I've already taken.

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **Event Listing** | Display all enabled events for today, sorted by time (ascending) | ✅ Implemented |
| **Visual Hierarchy** | Show icon emoji, time, event name, medication list | ✅ Implemented |
| **Completion Indicator** | Checkbox/toggle shows if event was taken today | ✅ Implemented |
| **Visual Distinction** | Completed events show strikethrough text, greyed colors | ✅ Implemented |
| **No Events State** | Display friendly message if no events are configured | ✅ Implemented |
| **Empty State** | Show "Sem eventos hoje" message with CTA to add first event | ✅ Implemented |
| **Real-Time Update** | UI auto-updates when events are added/modified (StateFlow-driven) | ✅ Implemented |

### Technical Implementation
- **Location:** `ui/dashboard/DashboardScreen.kt`
- **ViewModel:** `DashboardViewModel.kt` (exposes `events: StateFlow<List<EventEntity>>`)
- **Components:** 
  - `EventCard.kt` - Individual event display. Uses an **Edge-to-Edge Expanded Card** architecture (full screen, no borders) for editing and medication management.
  - `AddEventDialog.kt` - Used for creating *new* events with validation.
- **Reactive Binding:** Compose automatically recomposes on StateFlow emission.

---

## 3. Time Management (Add/Edit/Delete Events)

### Description
Users can create medication reminders at specific times, modify existing ones, or delete them entirely.

### User Story
> As a user, I want to add new medication times, edit their details, and remove times I no longer need.

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **Create Event** | User inputs time (HH:MM), name, and optional icon selection | ✅ Implemented |
| **Time Picker** | Native time picker dialog (platform default) | ✅ Implemented |
| **Name Input** | Free-text input for event label (e.g., "Café da Manhã") | ✅ Implemented |
| **Auto-Icon** | Clock emoji automatically assigned based on time (e.g., 🕐 for 1 AM) | ✅ Implemented |
| **Edit Event** | Modify time, name, or enable/disable status | ✅ Implemented |
| **Edit Triggers Reschedule** | Changing time auto-reschedules the alarm | ✅ Implemented |
| **Delete Event** | Remove event from database and cancel associated alarm | ✅ Implemented |
| **Undo Not Available** | Deletion is permanent (future enhancement: soft deletes with recovery) | ⚠️ By Design |
| **Persistence** | All changes instantly saved to Room database | ✅ Implemented |

### Technical Implementation
- **Location:** `ui/dashboard/AddEventDialog.kt` (dialog component)
- **ViewModel Methods:**
  - `addEvent(label: String, time: String)` 
  - `updateEvent(event: EventEntity, newTitle: String, newTime: String)`
  - `deleteEvent(event: EventEntity)`
- **Database Layer:** `EventDao.kt` (CRUD operations via Room)
- **Entity Schema:** `EventEntity.kt` (id, title, time, medications[], isEnabled, isTakenToday, icon)

---

## 4. Medication Management (Add/Remove Medications)

### Description
Within each event, users can maintain a list of medications they need to take at that time.

### User Story
> As a user, I want to associate multiple medications with each time slot, so my notification tells me what exactly to take.

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **Add Medication** | Input free-text medication name directly inside the expanded event card | ✅ Implemented |
| **Input Chips** | Medications displayed as interactive `InputChip` (expanded) or `AssistChip` (compact) | ✅ Implemented |
| **Validation** | Prevent empty names and enforce 30-character limit with character counter | ✅ Implemented |
| **Remove Medication** | Tap "Close" icon on medication chip in the expanded card | ✅ Implemented |
| **Empty Med List** | Event can have zero medications (icon alone serves as reminder) | ✅ Implemented |
| **Medication Count** | Visual counter shown during input (e.g. 12/30) | ✅ Implemented |
| **Sorting** | No specific medication sort order (maintained as insertion order) | ⚠️ By Design |
| **Persistence** | Medication list serialized as `List<String>` in Room via TypeConverter | ✅ Implemented |

### Technical Implementation
- **ViewModel Methods:**
  - `addMedication(eventId: String, medicationName: String)`
  - `removeMedication(eventId: String, index: Int)`
- **Data Layer:** `MedicationTypeConverter.kt` (converts List<String> ↔ JSON string for Room)
- **Entity Field:** `medications: List<String> = emptyList()`

---

## 5. Alarms & Notifications (Background Scheduling)

### Description
The app reliably schedules local notifications at user-specified times, even when the app is closed or device is in Doze Mode.

### User Story
> As a user, I want to receive reliable notifications at my medication times, even if I haven't opened the app or my phone is in sleep mode.

### Capabilities

| Feature                   | Behavior                                                                                                                 | Status |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------|--------|
| **Daily Scheduling**      | Alarm fires every 24 hours at the specified time                                                                         | ✅ Implemented |
| **Precise Timing**        | AlarmManager uses exact alarms when permitted                                                                            | ✅ Implemented |
| **Doze Mode Bypass**      | Uses `setExactAndAllowWhileIdle()` to fire through low-power states                                                      | ✅ Implemented |
| **Fallback Mode**         | On Android 12+, falls back to `setAndAllowWhileIdle()` if exact permission denied                                        | ✅ Implemented |
| **Auto-Reschedule**       | Changing event time automatically reschedules the alarm                                                                  | ✅ Implemented |
| **Cancel on Delete**      | Deleting an event cancels its associated alarm                                                                           | ✅ Implemented |
| ~~**Cancel on Disable**~~ | Disabling an event cancels the alarm (não passou nos testes de usabilidade e está sendo replanejada) | ⚠️ Not Yet Implemented |
| **Battery Efficient**     | Alarms are persistent via AlarmManager; no excessive CPU usage                                                           | ✅ Implemented |

### Technical Implementation
- **Location:** `background/AlarmScheduler.kt` (interface & implementation)
- **Receiver:** `background/AlarmReceiver.kt` (BroadcastReceiver for alarm triggers)
- **Helper:** `background/NotificationHelper.kt` (constructs & sends notifications)
- **ViewModel Integration:** `DashboardViewModel.scheduleEventAlarm(event)` calls scheduler

### Alarm Sequence Diagram

```
User: Add Event (15:00)
  ↓
DashboardViewModel.addEvent()
  ↓
EventRepository.insertEvent()
  ↓
EventDao.insertEvent() → Room Insert
  ↓
DashboardViewModel.scheduleEventAlarm(event)
  ↓
AlarmScheduler.scheduleAlarm()
  ├─ Create PendingIntent (broadcast)
  ├─ Calculate next trigger time (calendar math)
  ├─ Check Android version + permission
  ├─ Call AlarmManager.setExactAndAllowWhileIdle() or setAndAllowWhileIdle()
  └─ Return to caller

[Time passes... 15:00 arrives]
  ↓
OS AlarmManager triggers PendingIntent
  ↓
AlarmReceiver.onReceive() is called
  ├─ Extract title & message from Intent extras
  ├─ Call NotificationHelper.sendNotification()
  └─ Framework reschedules alarm for tomorrow 15:00

NotificationHelper.sendNotification()
  ├─ Build NotificationCompat.Builder
  ├─ Set channel (NOTIFICATIONS channel)
  ├─ Set vibration + sound
  ├─ Create PendingIntent to open MainActivity
  └─ NotificationManager.notify()
```

### Notification Content Template
```
Title:   "[Time] [Event Name]"
Body:    "Remédios: [Med1, Med2, ...]"
Sound:   Enabled
Vibration: Enabled
Channel:  "notifications" (important priority)
```

---

## 6. ~~Toggle Events (Enable/Disable)~~

> This feature failed usability testing and is being redesigned.

### Description
Users can temporarily disable events without deleting them, pausing notifications while keeping configuration intact.

### User Story
> As a user, I want to disable a medication reminder on weekends or temporarily pause it without losing my setup.

---

## 7. Daily Reset of Status

### Description
The `isTakenToday` flag (mark complete) resets to `false` at the start of each day.

### User Story
> As a user, I want my medication checklist to reset daily so I can mark today's items as complete separately from yesterday's.

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **Automatic Reset** | When app opens on a new day, all `isTakenToday` flags reset | ✅ Implemented |
| **One-Time per Day** | Reset happens only once per calendar day | ✅ Implemented |
| **Transparent to User** | User sees fresh checklist each morning; no manual action needed | ✅ Implemented |
| **History Tracking** | (Future) Option to view past completion history | ⚠️ Not Yet Implemented |

### Technical Implementation
- **Location:** `MainActivity.kt` or `DashboardViewModel.init()`
- **Logic:** Check if `SharedPreferences` date differs from today; if yes, batch update all events to `isTakenToday = false`
- **Efficiency:** Single Room query + batch update (not individual updates)

---

## 8. Widget (Home Screen Quick View)

### Description
An interactive widget on the device home screen displays today's medications without opening the app.

### User Story
> As a user, I want to quickly see my medication schedule and mark items complete directly from my home screen without opening the app.

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **Widget Display** | Shows list of today's events (time, name, medications) | ✅ Implemented |
| **Glance Framework** | Built with Jetpack Glance (Compose-based, modern) | ✅ Implemented |
| **Auto-Update** | Widget refreshes when app database changes | ✅ Implemented |
| **Click Action** | Tapping widget opens app to Dashboard | ✅ Implemented |
| **Visual Consistency** | Uses same color scheme and typography as app | ✅ Implemented |
| **Completion Indicator** | Shows strikethrough for completed events | ✅ Implemented |
| **Empty State** | Shows "Sem remédios hoje" if no events configured | ✅ Implemented |
| **Interactive Check** | (Future) Mark complete directly from widget without opening app | ⚠️ Not Yet Implemented |
| **Resizable** | Widget adapts to various sizes (4x2, 4x3, etc.) | ⚠️ Limited |
| **Dark Mode** | (Future) Support for dark theme variant | ⚠️ Not Yet Implemented |

### Technical Implementation
- **Location:** `widget/MedicationWidget.kt` (GlanceAppWidget subclass)
- **Receiver:** `widget/MedicationWidgetReceiver.kt` (Widget provider declaration)
- **Manifest:** `AndroidManifest.xml` includes widget provider + permissions
- **Update Trigger:** `EventRepository.updateWidgets()` calls `MedicationWidget.updateAll(context)`
- **Database Access:** Synchronous read via `database.eventDao().getAllEvents().first()`

### Widget Architecture Diagram

```
Room Database
  ↓
EventRepository (repository layer)
  ├─ On insertEvent() → updateWidgets()
  ├─ On updateEvent() → updateWidgets()
  └─ On deleteEvent() → updateWidgets()
  ↓
MedicationWidget.updateAll(context)
  ├─ Glance framework fetches DB snapshot
  ├─ Renders @Composable WidgetEventItem() for each event
  └─ Pushes UI update to widget host
  ↓
Device Home Screen Widget
  ├─ Displays pill icon, time, event name
  ├─ On click → Start MainActivity (with deep-link support)
  └─ User sees live list without opening app
```

---

## 9. Notification Permissions

### Description
The app requests necessary OS permissions for notifications and alarm scheduling.

> [!IMPORTANT]  
> **Alarm Permission Policy:** We strictly use `SCHEDULE_EXACT_ALARM`. The more restrictive `USE_EXACT_ALARM` was intentionally removed because it is restricted by Google Play to core alarm/calendar apps. Using it without meeting strict criteria results in Store rejection. Our implementation includes logic to fallback to inexact alarms if the exact permission is not granted.

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **POST_NOTIFICATIONS** | Request permission to display notifications (Android 13+) | ✅ Implemented |
| **SCHEDULE_EXACT_ALARM** | Request permission for exact alarm scheduling (Android 12+) | ✅ Implemented |
| **READ_EXTERNAL_STORAGE** | (Optional) For future features like photo reminders | ⚠️ Not Yet Implemented |
| **Permission Denial Handling** | App gracefully handles denial; alarms use inexact mode as fallback | ✅ Implemented |
| **Onboarding Flow** | Request permissions during initial setup if possible | ✅ Implemented |

### Technical Implementation
- **Manifest Declarations:** `AndroidManifest.xml` includes all required permissions
- **Runtime Requests:** Platform handles >= Android 13 permission prompts
- **Fallback Logic:** `AlarmSchedulerImpl.scheduleAlarm()` checks `canScheduleExactAlarms()` and adapts

---

## 10. Offline-First Architecture

### Description
The app functions entirely without internet connectivity; all data persists on-device.

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **No Network Required** | Core features (view, add, edit, alarm) work offline | ✅ Implemented |
| **Local Database** | SQLite Room database on device | ✅ Implemented |
| **Data Privacy** | User data never leaves the device | ✅ Implemented |
| **No Cloud Sync** | (By design) No automatic sync to cloud backends | ✅ Implemented |
| **App Updates** | (Future) Optional cloud-based feature updates | ⚠️ Not Yet Implemented |

---

## 11. Internationalization (i18n)

### Description
The app displays UI text in the user's system language (Portuguese or English).

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **Language Detection** | System locale (PT-BR or EN-US) auto-selected | ✅ Implemented |
| **String Resources** | All UI strings in `strings.xml` (defaults to PT-BR) | ✅ Implemented |
| **English Strings** | (Future) `strings-en.xml` for English variant | ⚠️ Planned |
| **Date/Time Localization** | Time format respects system settings | ✅ Implemented |

### Technical Implementation
- **Default Strings:** `res/values/strings.xml` (Portuguese)
- **Alternative Locale:** `res/values-pt-BR/strings.xml` (optional override)
- **Future English:** `res/values-en-US/strings.xml` (to be created)

---

## 12. Accessibility Features

### Description
The app is designed to be usable by diverse populations, including elderly users with varying technical comfort.

### Capabilities

| Feature | Behavior | Status |
|---------|----------|--------|
| **Screen Reader Support** | High-quality semantic descriptions for TalkBack, including merged card status | ✅ Implemented |
| **High Contrast** | Text colors meet WCAG AA standards (dark on light) | ✅ Implemented |
| **Large Touch Targets** | Event cards and buttons sized for accessibility (minimum 48dp) | ✅ Implemented |
| **Text Sizing** | System text scaling respected (device-level font size settings) | ✅ Implemented |
| **Icon Clarity** | Emojis (🕐, 💊) used alongside text (not text-only) | ✅ Implemented |
| **Deep-Linking** | Automated scrolling and visual highlight when opening from widget or notification | ✅ Implemented |
| **Haptic Feedback** | Vibration on alarm to alert users even if sound is off | ✅ Implemented |

---

## Risk & Limitations

| Item | Current Status | Mitigation |
|------|-----------------|-----------|
| **Alarm Reliability on Custom ROMs** | Some devices with heavily modified Android fire alarms unreliably | Document known issues; recommend stock Android |
| **Doze Mode Edge Cases** | Certain device manufacturers' aggressive battery saver modes may suppress alarms | User can whitelist app in battery settings |
| **Widget Display Lag** | Widget update may lag 1-2 seconds after database change | Acceptable UX; prioritize reliability over speed |
| **No Recurring Rules** | Medications only repeat daily; no weekly or custom patterns yet | Planned for future phase |
| **Single User** | No multi-user/multi-profile support | By design; supports single user per device |

---

## Feature Roadmap (Future Phases)

### Phase 4 (Polish & Accessibility)
- [ ] Screen reader support (TalkBack)
- [ ] Dark mode theme variant
- [ ] History view (past medication completion logs)
- [ ] Widget interactive actions (mark complete from widget)

### Phase 5 (Advanced Scheduling)
- [ ] Recurring rules (weekly, custom days)
- [ ] As-needed medications (not time-based)
- [ ] Medication refill reminders
- [ ] Interaction with wearables (notification on smartwatch)

### Phase 6 (Multi-User & Cloud)
- [ ] Caregiver mode (one person managing multiple users' schedules)
- [ ] Optional cloud backup with end-to-end encryption
- [ ] Family sharing

---

## Matrix: Features by Audience

| Feature | Users | Developers | Product | QA |
|---------|-------|-----------|---------|-----|
| Onboarding | 🔴 High Priority | 🟢 Reference | 🔴 High Priority | 🟢 Full Coverage |
| Dashboard | 🔴 High Priority | 🔴 High Priority | 🔴 High Priority | 🔴 High Priority |
| Time Management | 🔴 High Priority | 🟢 Reference | 🔴 High Priority | 🟢 Full Coverage |
| Medication Management | 🟢 Core Use | 🟢 Reference | 🟢 Core Use | 🟢 Full Coverage |
| Alarms/Notifications | 🔴 Critical | 🔴 Complex | 🔴 Critical | 🔴 Extended Testing |
| Widget | 🟢 Convenience | 🟢 Reference | 🟢 Differentiator | 🟢 Full Coverage |
| Permissions | 🟡 Background | 🟢 Reference | 🟡 Background | 🟢 Smoke Test |
| Offline Capability | 🟢 Assumed | 🟢 Designed In | 🟢 Feature | 🔴 Requires Test Plan |
| i18n | 🟡 Localization | 🟢 Extensible | 🟡 Future Market | 🟡 Smoke Test |
| Accessibility | 🔴 Inclusive Design | 🟢 Extensible | 🟡 Future | 🟡 Planned |

**Legend:** 🔴 = High/Critical | 🟢 = Important/Medium | 🟡 = Nice-to-Have/Low


