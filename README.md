# MoneyMitra 💰

MoneyMitra is an Android application designed to help users track and manage their personal finances in a simple, visual, and intuitive way.  
The app focuses on clean UI, meaningful insights, and real-time data handling using Firebase.

---

## 🚀 Features

### 🔹 Investment Module ✅ (Completed)
- Add, edit, and delete investments
- Firebase Firestore-backed persistent storage
- Interactive donut chart (MPAndroidChart)
- Real-time **chart ↔ card synchronization**
- Color-coded investments with smooth animations
- Clickable chart slices with card highlighting
- Clean empty state handling
- Polished UI inspired by modern finance apps

---
### Expense Module ✅ (Completed)
- Add, edit, and delete expenses
- Category-wise expense tracking
- Monthly budget setting (per category)
- Real-time budget exceeded notifications
- Daily expense reminder notifications
- Interactive donut chart (MPAndroidChart)
- Time filters: Today / Week / Month
- Firebase Firestore backend integration
  ---
- ### 🎯 Goals Module ✅ (Completed)
The Goals module allows users to plan, track, and manage their financial goals with real-time progress updates.

**Key Features:**
- Create financial goals with a target amount
- Edit and delete existing goals
- Link goals to investments using investment IDs
- Automatic progress calculation based on linked investment amount
- Supports decimal progress (e.g. 0.1%, 0.5%) for accurate tracking
- Smooth animated progress bar with easing
- Clean empty-state handling with fade-in transitions
- No UI flicker during loading
- Polished UX with press animations on buttons, cards, and icons


## 🛠 Tech Stack
- **Language:** Java
- **UI:** XML (Material Design)
- **Database:** Firebase Firestore
- **Authentication:** Firebase Auth (Google Sign-In supported)
- **Charts:** MPAndroidChart

---

## 📱 Screens Implemented
- Authentication (Login / Signup)
- Dashboard
- Investment Tracker (Complete)
- Expense Tracker (Complete)
- Financial Goals Tracker (Complete)

---

## 📌 Upcoming Modules
- Assets & Liabilities
- Financial Calculators
- AI-based insights (planned)

---

## 🎯 Project Focus
MoneyMitra is built with an emphasis on:
- Clean architecture
- Smooth animations and interactions
- Interview-ready, production-quality Android code

---

## 📂 Repository Structure
```text
app/
 ├── activities
 ├── adapters
 ├── models
 ├── repository
 ├── res/
