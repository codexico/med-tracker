# 📊 Product Guide: Meus Remedinhos (Med Tracker)

> Documentation for product managers, analysts, QA, and stakeholders regarding application features, user flows, and capabilities.

---

## 1. Product Overview

### What is Meus Remedinhos?

**Meus Remedinhos** is a native Android mobile application that helps users:

✅ **Organize** medications into specific time slots (e.g., breakfast, lunch, dinner).  
✅ **Receive reliable reminders** at scheduled times.  
✅ **Track** which medications have already been taken today.  
✅ **Visualize** their daily routine via home screen widgets.

### Target Audience

| Group | Characteristics | Needs |
| :--- | :--- | :--- |
| **Elderly Users** | Lower tech familiarity; irregular memory. | Simple interface, large text, reliable reminders. |
| **Chronic Patients** | Multiple medications; complex schedules. | Clear organization, precise scheduling. |
| **Caregivers** | Manage medications for others. | Simple visibility, uncomplicated control. |

### Value Proposition

| Aspect | Differentiator |
| :--- | :--- |
| **Reliability** | Reminders work offline; 100% device-persistent (no synchronous cloud required). |
| **Privacy** | Data never leaves the phone; no external tracking. |
| **Simplicity** | Only 3 main actions: Add time, list medications, mark as taken. |
| **Accessibility** | High-contrast interface, large text, simple gestures. |
| **Cost-Free** | Free and open-source. |

---

## 2. Core User Flows

### 2.1 First Run Experience (Onboarding)
A clean start with interactive tooltips guiding the user toward creating their first medication schedule.

### 2.2 Daily Schedule Management (Dashboard)
The primary screen where users view today's medication schedule and manage completion status. Actions include:
- **Tapping checkbox**: Marks/unmarks event as taken.
- **Tapping "Edit"**: Opens advanced editing options.
- **FAB (+)**: Opens options to add new time slots.

### 2.3 Managing Medications
Users can associate multiple medications with each time slot. Notifications include the specific medication names to be taken at that time.

### 2.4 Notifications & Alarms
Reliable system-level alerts that fire even if the app is closed or the device is in power-saving mode. Includes vibration and sound feedback.

### 2.5 Home Screen Widget
A quick-view component showing the next scheduled events without needing to open the app. Supports deep-linking directly to a specific event for easy tracking.

---

## 3. Requirements & Limitations

### System Requirements
- **Platform**: Android only.
- **Version**: Android 14+ (API 34+).
- **RAM**: Minimum 2 GB.
- **Storage**: ~50 MB free space.

### Known Limitations
- **No Multi-Device Sync**: Data is stored locally per device.
- **No Cloud Backup**: Privacy-first design (Android system backup recommended).
- **Daily Repetition Only**: Current version supports daily recurring slots (weekly/custom rules planned).

---

## 4. Success Metrics & KPIs

| Metric | Target | Rationale |
| :--- | :--- | :--- |
| **Daily Active Users (DAU)** | >80% | Medication is a daily necessity. |
| **Alarm Delivery Rate** | 99%+ | Critical for patient health. |
| **Mean Time to Open** | <2 sec | Performance expectation. |
| **App Rating** | >4.5 ⭐ | perceived quality indicator. |

---

## 5. Roadmap

- **Phase 1 (MVP)**: Core tracking, alarms, and widget. ✅
- **Phase 2 (UX Polish)**: Themes, adaptive widgets, more languages.
- **Phase 3 (History)**: 90-day adherence reports and data visualization.
- **Phase 4 (Advanced)**: Recurring rules, "as-needed" meds, and wearable integration.
