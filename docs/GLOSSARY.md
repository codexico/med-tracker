# 📖 Project Glossary: Meus Remedinhos

> Standard definitions and terminology for the application domain.

---

## Data Entities

### Event (or Schedule Slot)
The primary organization unit. An Event has a title (e.g., "Breakfast"), a specific time, and a list of associated medications.

### Medication
An individual item registered within an **Event**. It represents the specific pill, drop, or supplement to be taken.

### Event Status
- **Pending**: The initial state at the start of each day.
- **Taken**: The state after a user confirms they have taken the medication. Marked with a strikethrough in the UI.

### Daily Reset
The automatic logic that resets all "Taken" statuses back to "Pending" at 00:00 (Midnight) every day.

---

## UI Components

### Dashboard
The main app screen displaying all **Events** chronologically.

### Event Card
The visual container for a single **Event**.
- **Compact View**: Shows the title, time, and medication preview.
- **Expanded View**: Used for editing name, time, and managing the medication list.

### Input Chip
Interactive elements used inside the Expanded Card to display medications. Clicking one triggers the edit form.

### Home Widget
A small preview of the daily schedule added to the Android Home Screen.

---

## Technical Terms

### Alarm / Reminder
A system notification scheduled via `AlarmManager` for a specific Event's time.

### Deep-link
A specialized URL (e.g., `meusremedinhos://event/{id}`) that opens the app from a widget or notification directly to a specific Event.

### Highlight
A temporary visual background color applied to a card when navigated via a Deep-link.

### Seeding
The automatic creation of default events (e.g., Wake Up, Lunch) during the very first app launch.
