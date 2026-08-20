# 🛠️ Technical Plan: Inventory Control & Medication Flow

> Implementation strategy for the medication inventory system and transition to a timeline-based history model.

---

## 1. Core Philosophy: Physical Integrity
The physical stock is the "master of truth". 
- **Deduction on "Taken" only**: Stock is only decreased when the user explicitly marks an event as taken.
- **Avoid Desync**: Automatic subtraction is avoided to prevent trust issues where the app and physical box don't match.

---

## 2. Paradigm Shift: Daily Reset to Timeline
Transitioning from a nightly status wipe to a permanent `DoseHistory` model.

### New Approach:
1.  **DoseHistory (Entity)**: New database table tracking every dose.
    - `id`, `eventId`, `medicationId`, `timestamp`, `status`.
2.  **Overdue Events**: Unmarked events from "yesterday" persist on the Dashboard as **"Pending Registration"**.
    - User options: "I took it (late)" (deducts stock) or "Skip dose" (logs as skipped, clears alert).

---

## 3. Key Technical Architecture

### A. Data Layer (Repositories)
- **Single Source of Truth**: Room Database for inventory levels.
- **Main-Safety**: All calculations (e.g., `currentStock - dosage`) must happen on `Dispatchers.IO`.
- **Atomic Operations**: Use a `Mutex` in the repository to prevent race conditions during concurrent stock updates.

### B. Background Tasks (WorkManager)
- **Periodic Audits**: `PeriodicWorkRequest` to check stock levels and trigger notifications while the app is in the background.
- **Durable Updates**: User-triggered updates should use a CoroutineScope tied to the Application lifecycle, ensuring completion even if the UI is closed immediately.

### C. UI Patterns (Material 3)
- **Visual Hierarchy**: Use `error` or `tertiary` color roles for critical stock items.
- **Alert Dialogs**: High-priority interruptions for "Out of Stock" scenarios with clear CTAs (e.g., "Register Purchase").

---

## 4. Implementation Steps

1.  **Room Migration**: Upgrade database schema with new entities (`Medication`, `EventMedication`, `DoseHistory`).
2.  **Repository Refactor**: Implement the `Mutex` protected stock subtraction logic.
3.  **Dashboard Update**: Switch from `isTakenToday` boolean to checking `DoseHistory` for the current date.
4.  **Stock Management UI**: Build the inventory screen and integrate stock inputs into the medication form.
