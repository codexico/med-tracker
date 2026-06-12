# 📋 Project Overview: Meus Remedinhos (Med Tracker)

> **A native Android application for reliable medication tracking and reminders.** Evolved from PWA → React Native → **Kotlin Native** to unlock robust background alarms, native widgets, and offline-first persistence.

---

## 🎯 Executive Summary

**Meus Remedinhos** is a medication reminder application designed for:
- **Elderly users** managing daily medication schedules
- **Chronic disease patients** coordinating multiple medications
- **Caregivers** tracking simple recurring schedules

### Core Promise
✅ **100% reliable local notifications** (no internet required)  
✅ **Simple, high-contrast UI** (accessible to diverse user groups)  
✅ **Offline-first architecture** (100% privacy - data never leaves device)  
✅ **Native widgets** for quick at-a-glance status without opening the app

---

## 📱 Current Status

| Aspect | Details |
|--------|---------|
| **Platform** | Android (Native Kotlin) |
| **Target SDK** | Android 14+ (API 34+) |
| **Language** | Kotlin + TypeScript (original RN codebase as reference) |
| **UI Framework** | Jetpack Compose + Navigation Compose |
| **Database** | Room SQLite (with KSP2) |
| **Persistence** | 100% offline-capable, on-device only |
| **Background Tasks** | AlarmManager for precise scheduling |
| **Widgets** | Jetpack Glance (modern Compose-based) |
| **License** | MIT (Open Source) |

---

## 🏗 Evolution: Why Kotlin Native?

### Version 1: PWA (Progressive Web App)
**Status:** ❌ **Archived**
- **Problem:** Service Workers cannot schedule reliable background notifications on mobile browsers (iOS/Android restrictions).
- **Outcome:** Unreliable reminders made the app unsuitable for healthcare use.

### Version 2: React Native (Expo)
**Status:** ✅ **Production** → Ready for migration
- **Achievements:** 
  - Local notifications via `expo-notifications`
  - SQLite persistence via `expo-sqlite`
  - Widget support attempted (unstable third-party plugin)
- **Limitation:** Widget support was fragile; background alarm precision had edge cases on newer Android versions with Doze Mode.

### Version 3: Kotlin Native (Current)
**Status:** 🚀 **Active Development**
- **Rationale:**
  1. **AlarmManager** provides millisecond-precision recurring alarms that survive Doze Mode.
  2. **Jetpack Glance** allows writing widgets in Compose (not XML RemoteViews).
  3. **Room + KSP2** offers type-safe, efficient database access.
  4. **Full control** over system capabilities (exact alarms, SCHEDULE_EXACT_ALARM permission).
- **Trade-off:** More code required, but significantly more reliable.

---

## 👥 Stakeholders & Documentation Roadmap

| Role | Primary Document | Questions Answered |
|------|---------------------|-------------------|
| **Product Managers / Analysts** | [`PRODUCT_GUIDE.md`](PRODUCT_GUIDE.md) | What are the features? How do users interact? What's the roadmap? |
| **Developers** | [`DEVELOPER_GUIDE.md`](DEVELOPER_GUIDE.md) | How do I build/test? Where's the code? What patterns do we use? |
| **QA / Testers** | [`FEATURES.md`](FEATURES.md) + [`PRODUCT_GUIDE.md`](PRODUCT_GUIDE.md) | What should I test? What are the edge cases? |
| **AI / Code Agents** | All docs + [`AI_CONTEXT.md`](AI_CONTEXT.md) | How do I refactor safely? What dependencies are critical? |

---

## 🎨 Visual Identity

Inherited from the original React Native implementation (`constants/theme.ts`):

```
Primary Color:       #8B6F47 (Warm Earth Tone)
Secondary Color:     #D4A574 (Light Gold)
Background:          #F0D4BD (Cream/Beige)
Surface:             #FFFFFF (White)
Text Primary:        #2D241B (Dark Brown)
Text Secondary:      #6D5D4B (Medium Brown)
Status Off:          #767577 (Grey)
Error:               #D32F2F (Red)
```

**Design Philosophy:** High-contrast, senior-friendly (large touch targets, clear visual hierarchy).

---

## 📦 Tech Stack at a Glance

### Build & Tooling
- **Language:** Kotlin 2.x with coroutines
- **Build System:** Gradle 8.x (Kotlin DSL) + AGP 9.0+
- **JDK:** Java 17 (OpenJDK)
- **Code Generation:** KSP2 (replaces KAPT)

### Framework & UI
- **UI:** Jetpack Compose (declarative, reactive layer)
- **Navigation:** Navigation Compose (type-safe routing)
- **Theme:** Material Design 3 compatible

### Data & Persistence
- **Database:** Room (Kotlin-friendly ORM)
- **Schema:** SQLite with TypeConverters for complex types
- **Transactions:** Full ACID compliance for medication event data

### Background & Notifications
- **Scheduling:** AlarmManager (exact alarms with Doze Mode bypass)
- **Notifications:** NotificationCompat (native Android notifications)
- **Broadcast Receiver:** Handles alarm triggers, spawns notifications

### Widgets
- **Widget Framework:** Jetpack Glance (modern, Compose-based)
- **Update Mechanism:** `updateAll()` triggered on database changes
- **Interaction:** Click actions redirect to app with deep-link support

### Architecture
- **Pattern:** MVVM (Model-View-ViewModel)
- **Reactive:** StateFlow for UI state management
- **Coroutines:** Structured concurrency for async operations

---

## 📊 Project Structure

```
Kotlin/
├── app/
│   ├── build.gradle.kts              # App module build config
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/franciscokahil/appMeusRemedinhos/
│   │   │   │   ├── MainActivity.kt    # App entry point
│   │   │   │   ├── ui/
│   │   │   │   │   ├── dashboard/     # Dashboard screens & components
│   │   │   │   │   ├── onboarding/    # First-run flow
│   │   │   │   │   └── MainNavigation.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/          # Room database layer
│   │   │   │   │   ├── repository/     # Data interface layer
│   │   │   │   │   └── model/
│   │   │   │   ├── background/
│   │   │   │   │   ├── AlarmScheduler.kt  # Alarm scheduling logic
│   │   │   │   │   ├── AlarmReceiver.kt   # Broadcast receiver for alarms
│   │   │   │   │   └── NotificationHelper.kt
│   │   │   │   ├── widget/
│   │   │   │   │   ├── MedicationWidget.kt
│   │   │   │   │   └── MedicationWidgetReceiver.kt
│   │   │   │   └── theme/               # Colors, typography
│   │   │   ├── res/
│   │   │   │   ├── values/strings.xml   # All UI strings (PT-BR)
│   │   │   │   ├── values/colors.xml
│   │   │   │   └── ...
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                        # JVM unit tests
│   │   └── androidTest/                 # Instrumented tests (emulator)
├── gradle/libs.versions.toml           # Centralized dependency versions
└── build.gradle.kts                     # Root gradle config
```

---

## 🚀 Key Capabilities

### For Users
1. **Onboarding:** First-time setup with pre-populated default meal times
2. **Dashboard:** View today's medication schedule at a glance
3. **Event Management:** Add/edit/delete medication reminders
4. **Medication Tracking:** Add multiple medications per event
5. **Toggle Events:** Enable/disable reminders without deleting data
6. **Mark Complete:** Check off taken medications for today
7. **Widget:** Quick-view widget on device home screen

### For Developers
1. **Reactive UI:** StateFlow-driven, auto-recomputing Compose layouts
2. **Type-Safe DB:** Room DAO with compile-time SQL verification
3. **Precise Scheduling:** AlarmManager with Doze Mode compatibility
4. **Testable Architecture:** Clean separation of concerns (MVVM)
5. **Offline Capable:** Zero network dependencies for core features

---

## 📋 Getting Started

### For Developers
- Read [`DEVELOPER_GUIDE.md`](DEVELOPER_GUIDE.md) for setup and local development
- Consult [`AI_CONTEXT.md`](AI_CONTEXT.md) for code generation guidelines

### For Product/QA
- Review [`FEATURES.md`](FEATURES.md) for complete feature descriptions
- Study [`PRODUCT_GUIDE.md`](PRODUCT_GUIDE.md) for user flows and capabilities
- Reference [`README.md`](../README.md) for quick technical overview

---

## 🔗 Reference Materials

- [React Native (Previous Implementation)](../RN/) → Use for feature parity, UI text, color schemes
- [Android Developer Docs](https://developer.android.com/) → Official guidelines for Compose, Room, AlarmManager
- [Jetpack Glance Widget Guides](https://developer.android.com/develop/ui/compose/glance) → Widget development

---

## ✅ Maintenance & Updates

**Last Updated:** 2026-06-11  
**Maintained By:** Development Team  
**Review Cycle:** Quarterly or after major features  

> **Note:** This document should be reviewed whenever:
> - Major architecture changes occur
> - New platform targets are added
> - Dependency versions are significantly upgraded
> - User base significantly changes


