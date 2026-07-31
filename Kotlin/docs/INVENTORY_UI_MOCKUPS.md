# 📱 UI Mockups: Inventory Control

> Conceptual representations of how the new inventory features will look in the app.

---

## 1. Dashboard with Low Stock Warning
*A persistent but non-intrusive banner appears when items need restocking.*

```text
┌──────────────── Dashboard ──────────────────┐
│ Meus Remedinhos                    [📦 Stock] │
│                                               │
│  ⚠️ Low Stock: Aspirin (4 left)      [Refill] │
│  ──────────────────────────────────────────── │
│                                               │
│  🍳 08:00 • Breakfast                         │
│  ├─ 1 💊 Aspirin                              │
│  ├─ 1 💊 Vitamin D                            │
│  ☑️ [Marked Taken]                            │
│                                               │
│  🍴 20:00 • Dinner                            │
│  ├─ 1 💊 Aspirin                              │
│  ☐ [Mark as Taken]                            │
│                                               │
└───────────────────────────────────────────────┘
```

## 2. Inventory / "Stock" Screen
*Centralized management for all medication quantities.*

```text
┌────────────── Inventory ────────────────────┐
│ [←] Stock Management                    [+]   │
│                                               │
│  [🔍 Search medications...]                  │
│                                               │
│  💊 Aspirin                                   │
│     24 pills remaining                        │
│     Ends in: 12 days                          │
│     [ Edit Stock ]                            │
│                                               │
│  💊 Vitamin D                                 │
│     105 pills remaining                       │
│     Ends in: 3 months                         │
│     [ Edit Stock ]                            │
│                                               │
│  ⚠️ Metformin                                 │
│     2 pills left (CRITICAL)                   │
│     [ Register Purchase ]                     │
│                                               │
└───────────────────────────────────────────────┘
```

## 3. Medication Form (Edit/Add)
*Integrating stock input directly into the existing medication flow.*

```text
┌────────── Edit Medication ──────────────────┐
│                                             │
│  Name: [ Aspirin ______________________ ]   │
│                                             │
│  Dosage: [ 1.0 ]  Unit: [ 💊 Pill ▼ ]       │
│                                             │
│  ─── Inventory ───                          │
│                                             │
│  Current Stock: [ 24 ]                      │
│  Alert me when stock is: [ 5 ]              │
│                                             │
│  [ Cancel ] [ Remove ] [ Save ]             │
└─────────────────────────────────────────────┘
```
