# 📋 Task Backlog: Meus Remedinhos

> Tracking upcoming development stages, organized by priority and status.

---

## 🟢 High Priority (Next Sprint)

### Task: Inventory Control & Medication Entities
**Status: Planning 📋**
- [ ] Implement `Medication` as a Room entity for centralized stock management.
- [ ] Add `currentStock` and `lowStockThreshold` fields to the database.
- [ ] Create `DoseHistory` table to track every pill taken.
- [ ] Implement logic to subtract from stock when an event is marked "Taken".
- [ ] Create UI for low-stock warnings (banners/notifications).
- [ ] Design and implement the "Stock Management" screen.

---

## 🟡 Medium Priority

### Task: UI Refinement & Interaction
**Status: Done ✅**
- [x] Edge-to-edge expanded cards for focused editing.
- [x] Auto-save logic for medication inputs.
- [x] FAB visibility management during editing.
- [x] Home screen widget deep-linking and highlight feedback.

---

## ⚪ Low Priority (Roadmap)

- [ ] **Dark Mode**: Support for system-wide dark theme.
- [ ] **Wear OS**: Basic medication list for smartwatches.
- [ ] **Adherence Reports**: 30/90-day charts based on history logs.
- [ ] **Interactive Widgets**: Mark as taken directly from the home screen.
