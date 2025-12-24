# Product Requirements Document (PRD)

## 1. Introduction & Problem Statement
**Project Name:** Meus Remedinhos (Med Tracker) :pill:

### Problem
Managing daily medication can be challenging, especially for:
- Elderly users with memory lapses.
- Patients with chronic conditions taking multiple meds.
- Caregivers managing simple schedules.

Existing solutions in the Progressive Web App (PWA) space suffer from unreliable notifications on iOS/Android (due to browser restrictions), making them ineffective for critical health reminders.

### Solution
A **React Native (Expo)** mobile application that works **offline-first**, provides **reliable local notifications**, and offers a **simple, high-contrast interface** accessible to diverse users.

---

## 2. Goals & Success Metrics
### Primary Goal
- Provide reliable, on-time local notifications for medication events even when the app is closed or offline.

### Secondary Goals
- Offline-first architecture (no internet required for core functions).
- Simple, "senior-friendly" UI (high contrast, large touch targets).
- Migration of existing PWA features to a robust mobile experience.

### Success Metrics
- **Reliability:** Notifications fire 100% of the time on scheduled intervals (Daily).
- **Usability:** User can add a medication event (Time + Name) in < 3 clicks.
- **Engagement:** Users mark medications as "Taken" daily.

---

## 3. User Stories
| Priority | Role | Story | Acceptance Criteria |
| :--- | :--- | :--- | :--- |
| **P0** | User | I want to be notified when it's time to take my meds. | Notification appears with sound/vibration daily at set time. Content includes med names. |
| **P0** | User | I want to see a list of today's medications. | Dashboard lists events ordered by time. Visual indicator for "Completed" vs "Pending". |
| **P0** | User | I want to mark a medication as taken. | Tapping an event toggles its status. Visual feedback (strikethrough/check). |
| **P1** | User | I want to configure my schedule (add/edit/remove events). | Onboarding/Settings screen allows modifying events, times, and medication lists. |
| **P1** | User | I want to enable/disable specific events (e.g., weekends off). | Toggle switch on events to pause notifications without deleting data. |
| **P2** | User | I want a clear distinction between active & inactive settings. | Switches use high-contrast colors (Grey vs Primary Color). |

---

## 4. Functional Requirements

### 4.1 Notifications (Core)
- **Local Only:** No push servers. Uses `expo-notifications`.
- **Triggers:**
    - Fires daily at specific `HH:MM`.
    - Reschedules automatically if Time, Enabled status, or Medication List changes.
    - **No immediate trigger** on simple updates (avoid spam).
- **Content:** Title("Hora do Remedinho!"), Body("Tomar: [Lista de Meds]").

### 4.2 Data Management
- **Persistence:** `expo-sqlite` for structured event data.
- **Schema:**
    - `Event`: { id, label, time, icon, enabled, medications[] }
    - `History`: (Future Scope) Track past completion.
- **Offline:** 100% functional without network.

### 4.3 Onboarding
- **First Run:** Guide user to set up initial schedule.
- **Defaults:** Pre-populate common meals (Breakfast, Lunch, Dinner).
- **Skip:** User can skip setup and use defaults.

---

## 5. Technical Requirements

### 5.1 Tech Stack
- **Framework:** React Native + Expo (Managed Workflow).
- **Language:** TypeScript.
- **Storage:** `expo-sqlite`.
- **Navigation:** `expo-router` (File-based routing).
- **Styling:** `react-native` StyleSheet + Custom Theme Tokens (`constants/theme.ts`).

### 5.2 Constraints
- **Platform:** Android (Primary focus), iOS (Secondary).
- **Internet:** Not required except for app updates.
- **Privacy:** No cloud storage. User data stays on device.
- **License:** Open Source (MIT).

---

## 6. UX/UI Guidelines
- **Theme:**
    - Primary: `#8b6f47` (Earth/Warm tone).
    - Background: `#f0d4bd`.
    - Contrast: High contrast for text and active states.
- **Components:**
    - use `vector-icons` (Ionicons, Fontisto) for visual recognition (Pills, Sun, Moon).
    - **Switch:** Native toggles for settings (Grey #767577 OFF / Primary ON).
    - **Time Picker:** Native OS picker for familiarity.

---

## 7. Milestones
- [x] **Phase 1: Migration:** Port PWA logic to Expo, setup Navigation & Database.
- [x] **Phase 2: Core Features:** CRUD Events, Persistent Storage, Basic UI.
- [x] **Phase 3: Notifications:** Reliable local scheduling, daily repeats.
- [ ] **Phase 4: Polish:** Smart updates, History tracking, Accessibility improvements.
- [ ] **Phase 4: Polish:** Accessibility improvements, rigorous testing on devices.
