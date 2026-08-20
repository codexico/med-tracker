# 🎯 Features & Capabilities: Meus Remedinhos

> Complete feature inventory with user stories, implementation details, and current status.

---

## 1. Smart Onboarding
Interactive first-run experience that guides the user to set up their first medication reminder.
- **Capabilities**: Friendly empty state, pulsating tooltips, and manual presets (Breakfast, Lunch, etc.).
- **Technical**: Uses `rememberTooltipState` and `LaunchedEffect` for dynamic guidance.

## 2. Dynamic Medication Schedule (Dashboard)
A chronological view of all medications scheduled for the current day.
- **Capabilities**: 
  - **Mark as Taken**: Single-tap checkbox to mark a slot as complete.
  - **Visual Feedback**: Strikethrough and faded colors for completed tasks.
  - **Quick Edit**: Instant expansion of cards for modification.
- **Technical**: StateFlow-driven UI ensures real-time updates when data changes.

## 3. Advanced Medication Management
Detailed control over what medications are associated with each time slot.
- **Capabilities**:
  - **New Display Format**: Medications shown as `[Qty] [Emoji] [Name]` (e.g., `1 💊 Aspirin`).
  - **Interactive Editing**: Click on any medication chip to edit its details (name, quantity, unit).
  - **Custom Units**: Support for standard units (Pill, Drops, mg, etc.) and custom text units.
- **Technical**: Centralized `Medication.displayName` logic and `MedicationUnit` object system.

## 4. Precise Alarms & Local Notifications
Guaranteed reminders that fire even when the phone is in sleep mode.
- **Capabilities**:
  - **Exact Timing**: Uses Android's `AlarmManager` for millisecond precision.
  - **Localized Text**: Notifications dynamically include medication names in the correct language.
  - **Doze Mode Bypass**: High-priority alarms that wake the device.
- **Technical**: Robust fallback logic for Android 12+ exact alarm permissions.

## 5. Home Screen Widget
Quick access to the medication schedule directly from the Android home screen.
- **Capabilities**:
  - **Sync-to-App**: Click any item to open the app directly to that medication event.
  - **Live Updates**: Refreshes automatically as soon as a change is made in the app.
- **Technical**: Built with **Jetpack Glance** for modern, Compose-like widget development.

## 6. Full Internationalization (i18n)
Native support for multiple languages.
- **Languages**: Portuguese (Default) and English.
- **Capabilities**: Units, labels, error messages, and accessibility descriptions are all translated.

## 7. Privacy & Offline-First
A commitment to user data security.
- **Capabilities**: Works 100% offline. No cloud accounts required.
- **Storage**: Data is stored locally in an encrypted Room database on the device.
