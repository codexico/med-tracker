# 🔄 Inventory User Flows (Happy Paths)

> Visualizing the primary interactions between the user and the medication inventory system.

---

## 1. Adding a New Medication to Inventory
*Context: The user buys a new box of medicine.*

1.  **Open Inventory Screen**: User navigates to the new "Stock" or "Inventory" section.
2.  **Add Entry**: User taps "Register Purchase" or "+".
3.  **Input Details**:
    *   Search/Type name (Autocomplete appears if medication exists).
    *   Enter total quantity (e.g., "30").
    *   Set alert threshold (e.g., "Remind me when 5 are left").
4.  **Confirm**: Stock is updated in the database.

---

## 2. Marking an Event as Taken
*Context: User hears the alarm and takes their morning pill.*

1.  **Notification/Dashboard**: User clicks "Taken" on the Breakfast card.
2.  **Logic Trigger**:
    *   System identifies medications in "Breakfast" (e.g., 1x Aspirin, 1x Vitamin).
    *   System checks if stock exists for both.
    *   System subtracts the dosage from `Medication.currentStock`.
    *   System creates an entry in `DoseHistory`.
3.  **Low Stock Check**:
    *   If Aspirin stock is now 4 (Threshold was 5), a warning banner appears on the Dashboard.
    *   A notification is scheduled to remind the user to buy more.

---

## 3. Adjusting Inventory Manually
*Context: User dropped a pill on the floor and needs to correct the count, or simply wants to update stock during medication setup.*

**Option A: Via Stock Management Screen**
1.  **Edit Medication**: User taps the medication in the Inventory list.
2.  **Correction**: User changes "24 pills remaining" to "23".
3.  **Save**: Database is updated without creating a `DoseHistory` entry.

**Option B: Via Event/Medication Form**
1.  **Open Event**: User expands an event card on the Dashboard.
2.  **Edit/Add Medication**: User clicks a medication chip or starts adding a new one.
3.  **Inventory Fields**: User updates the "Current Stock" or "Alert Threshold" fields directly in the medication form.
4.  **Save**: Medication entity is updated in the database.

---

## 4. Handling Overdue Reminders
*Context: User forgot to mark yesterday's dinner.*

1.  **New Day**: Dashboard shows "1 Pending Registration" from Yesterday.
2.  **User Action**: Taps "I took it (late)".
3.  **Stock Update**: Stock is subtracted retrospectively, and `DoseHistory` is saved with the correct original event date but current submission time.
