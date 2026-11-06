# Monexi

A personal finance Android application built natively with **Kotlin** and **Jetpack Compose**, following **Clean Architecture** and **MVVM** design patterns.

---

## Demo

[![Monexi Demo](https://img.youtube.com/vi/6fFqFGbdQvc/hqdefault.jpg)](https://youtube.com/shorts/6fFqFGbdQvc)

> Click the thumbnail to watch a walkthrough of the app on YouTube Shorts.

---

## Features

- **Authentication** — Email/password sign-up and login via Firebase Auth, with persistent session management
- **Dashboard** — Monthly summary with income/expense totals, running balance, and a native Canvas donut chart for spending breakdown by category
- **Statement Screen** — Full transaction history with month selector, real-time search, and quick filters (All / Income / Expense)
- **Transactions** — Create and edit income or expense entries with category and payment method selection
- **Categories** — Custom categories with color picker and Material icon selector; archive/restore without losing transaction history
- **Payment Methods** — Manage payment methods; archive flow mirrors categories
- **Bank Accounts & Credit Cards** — Account balance tracking and credit card management (in progress on `dev`)
- **Cloud Sync** — Background sync between local Room database and Firebase Firestore, scoped per authenticated user
- **Theme** — Light/Dark theme toggle, persisted via DataStore Preferences

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Clean Architecture |
| Navigation | Navigation Compose |
| Local persistence | Room Database (KSP), DataStore Preferences |
| Cloud | Firebase Authentication, Cloud Firestore |
| Async | Kotlin Coroutines, Flow, StateFlow |
| Testing | JUnit 4, kotlinx-coroutines-test |
| CI | GitHub Actions (APK build & release) |

---

## Architecture

The project is structured in three layers following Clean Architecture principles:

```
UI (Compose)
    └── ViewModel  (StateFlow<UiState>)
            └── Use Case
                    └── Repository Interface
                            └── Room / Firestore implementation
```

**Data flow:**
- ViewModels expose a single immutable `StateFlow<UiState>` consumed by Compose screens
- Domain layer models (`Transaction`, `Category`, `PaymentMethod`) are free of framework annotations
- Mappers handle bidirectional conversion between Room `Entity`, Firestore documents, and domain models

**Use cases in domain layer:**

```
HomeViewModel
  ├── CalculateBalanceUseCase
  ├── CalculateIncomeUseCase
  └── CalculateExpenseUseCase
```

---

## Project Structure

```
app/src/main/java/com/moduxi/monexi/
├── data/
│   ├── local/
│   │   ├── MonexiDatabase.kt
│   │   ├── dao/            # CategoryDao, PaymentMethodDao, TransactionDao
│   │   ├── entity/         # CategoryEntity, PaymentMethodEntity, TransactionEntity
│   │   └── mapper/         # Bidirectional entity <-> domain mappers
│   └── repository/         # Room repository implementations + ThemeManager
├── domain/
│   ├── model/              # Category, PaymentMethod, Transaction, TransactionType
│   ├── repository/         # Repository interfaces (dependency inversion)
│   └── usecase/            # CalculateBalanceUseCase, CalculateIncomeUseCase, CalculateExpenseUseCase
├── presentation/
│   ├── auth/               # LoginScreen, RegisterScreen + ViewModels
│   ├── extract/            # ExtractScreen + ViewModel
│   ├── home/               # HomeScreen + ViewModel
│   ├── navigation/         # AppNavigation.kt
│   ├── settings/
│   │   ├── categories/     # CategoriesScreen, EditCategoriesScreen, ArchivedCategoriesScreen
│   │   └── payment/        # PaymentMethodScreen, EditPaymentMethodScreen, ArchivedPaymentMethodScreen
│   └── transaction/        # TransactionScreen + ViewModel
└── ui/theme/               # Color, Type, Theme, CategoryUiMapper
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17+
- A Firebase project with Authentication and Firestore enabled (optional — app runs without it in offline mode)

### Setup

```bash
git clone https://github.com/ribollitiago/monexi.git
cd monexi
```

If you want Firebase features, place your `google-services.json` inside `app/`. The app functions in offline-only mode without it.

### Build

```bash
# Debug build
./gradlew assembleDebug          # Linux/macOS
.\gradlew.bat assembleDebug      # Windows

# Run unit tests
./gradlew test
.\gradlew.bat test
```

---

## Android Config

```
applicationId : com.moduxi.monexi
minSdk        : 24  (Android 7.0+)
targetSdk     : 37
compileSdk    : 37
versionName   : 1.0
```

---

## Roadmap

- [ ] Bank account module — multiple independent balances (checking, savings, cash)
- [ ] Credit card module — credit limit, closing day, monthly statement view
- [ ] Inter-account transfers without affecting global income/expense totals
- [ ] Scheduled transaction notifications and bill reminders
- [ ] Explicit Room migrations replacing `fallbackToDestructiveMigration`
- [ ] Hilt dependency injection

---

## Author

**Tiago Ribolli Blasquez**
[linkedin.com/in/tiagoribolli](https://linkedin.com/in/tiago-ribolli-blasquez-060810244) · [github.com/ribollitiago](https://github.com/ribollitiago)
