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
- Firebase Firestore Backend Intehration

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
  
## Assets Module

Tracks and manages user assets with reliable storage, clean state handling, and category-wise visualization.

### Key Features
- Add, edit, and delete assets (name, category, value, notes)
- Persistent storage using **Firebase Firestore**
- Automatic total asset value calculation
- Category-wise distribution via **PieChart**
- Chart shown only when data exists (clean empty state)
- Smooth pie chart entry animation

 ### Liabilities Module

Manages user liabilities with real-time synchronization, accurate aggregation, and reliable state handling.

### Key Features

- Add, edit, and delete liabilities (name, category, amount, notes)

- Persistent storage using Firebase Firestore

- Automatic total liability calculation across all entries

- Real-time UI updates after add, edit, or delete actions

- Clean empty state handling when no liabilities exist

- Long-press contextual actions for edit and delete

- Amounts formatted using Indian number system commas

  ### 🧮 Financial Calculator Module ✅ (Completed)
The Financial Calculator module helps users quickly estimate common financial values directly inside the app without using external tools.

**Key Features:**
- Includes 5 calculators: SIP, EMI, FD, Loan and PF
- Reusable base calculator UI used across all calculators
- Dynamic headers and input hints for each calculator screen
- Accurate financial formulas implemented for real-world estimation
- Indian currency formatting with comma separators (₹)
- Input validation for empty fields and zero values
- Result card automatically hides when inputs are edited
- Smooth navigation from dashboard to individual calculators
- Clean and consistent Material UI design



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
- Assets Tracker (Complete)
-  Liabilities Tracker (Complete)
-  Financial Calculator (Completed)

---

## 📌 Upcoming Modules
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
