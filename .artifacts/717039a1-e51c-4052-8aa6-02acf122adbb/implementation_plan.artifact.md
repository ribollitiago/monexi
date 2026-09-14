# Rename Test Functions to camelCase

The goal is to fix the "Identifier not allowed in Android projects" error by renaming test functions that use backticks and spaces to camelCase. This error occurs because Android projects often enforce naming conventions that restrict certain characters in identifiers, even in unit tests.

## Proposed Changes

### [Test Components]

Summary: I will rename all test functions in the following files to use `camelCase` instead of `` `descriptive names with spaces` ``.

#### [MODIFY] [PaymentMethodViewModelTest.kt](file:///C:/Users/ribol/AndroidStudioProjects/Monexi/app/src/test/java/com/moduxi/monexi/presentation/settings/payment/PaymentMethodViewModelTest.kt)
- Rename `` `should add payment method` `` to `shouldAddPaymentMethod`
- Rename `` `should not add duplicated payment method` `` to `shouldNotAddDuplicatedPaymentMethod`
- Rename `` `should clear error when typing a new name` `` to `shouldClearErrorWhenTypingANewName`
- Rename `` `should not add blank payment method` `` to `shouldNotAddBlankPaymentMethod`
- Rename `` `should edit custom payment method` `` to `shouldEditCustomPaymentMethod`
- Rename `` `should not edit default payment method` `` to `shouldNotEditDefaultPaymentMethod`
- Rename `` `should delete custom payment method` `` to `shouldDeleteCustomPaymentMethod`
- Rename `` `should not delete default payment method` `` to `shouldNotDeleteDefaultPaymentMethod`
- Rename `` `should cancel editing payment method` `` to `shouldCancelEditingPaymentMethod`

#### [MODIFY] [CalculateBalanceUseCaseTest.kt](file:///C:/Users/ribol/AndroidStudioProjects/Monexi/app/src/test/java/com/moduxi/monexi/domain/usecase/CalculateBalanceUseCaseTest.kt)
- Rename `` `should calculate balance` `` to `shouldCalculateBalance`

#### [MODIFY] [CalculateExpenseUseCaseTest.kt](file:///C:/Users/ribol/AndroidStudioProjects/Monexi/app/src/test/java/com/moduxi/monexi/domain/usecase/CalculateExpenseUseCaseTest.kt)
- Rename `` `should calculate total expense` `` to `shouldCalculateTotalExpense`

#### [MODIFY] [CalculateIncomeUseCaseTest.kt](file:///C:/Users/ribol/AndroidStudioProjects/Monexi/app/src/test/java/com/moduxi/monexi/domain/usecase/CalculateIncomeUseCaseTest.kt)
- Rename `` `should calculate total income` `` to `shouldCalculateTotalIncome`

#### [MODIFY] [CategoriesViewModelTest.kt](file:///C:/Users/ribol/AndroidStudioProjects/Monexi/app/src/test/java/com/moduxi/monexi/presentation/settings/categories/CategoriesViewModelTest.kt)
- Rename `` `should add category` `` to `shouldAddCategory`
- Rename `` `should not add duplicated category for same type` `` to `shouldNotAddDuplicatedCategoryForSameType`
- Rename `` `should allow same category name for different type` `` to `shouldAllowSameCategoryNameForDifferentType`
- Rename `` `should not add blank category` `` to `shouldNotAddBlankCategory`
- Rename `` `should edit custom category` `` to `shouldEditCustomCategory`
- Rename `` `should not edit default category` `` to `shouldNotEditDefaultCategory`
- Rename `` `should delete custom category` `` to `shouldDeleteCustomCategory`
- Rename `` `should not delete default category` `` to `shouldNotDeleteDefaultCategory`
- Rename `` `should clear error on typing a new name` `` to `shouldClearErrorOnTypingANewName`
- Rename `` `should add category after duplicated category error` `` to `shouldAddCategoryAfterDuplicatedCategoryError`
- Rename `` `should cancel editing category` `` to `shouldCancelEditingCategory`

#### [MODIFY] [TransactionViewModelTest.kt](file:///C:/Users/ribol/AndroidStudioProjects/Monexi/app/src/test/java/com/moduxi/monexi/presentation/transaction/TransactionViewModelTest.kt)
- Rename `` `should save transaction` `` to `shouldSaveTransaction`
- Rename `` `should not save transaction without description` `` to `shouldNotSaveTransactionWithoutDescription`
- Rename `` `should not save transaction without amount` `` to `shouldNotSaveTransactionWithoutAmount`
- Rename `` `should not save transaction without category` `` to `shouldNotSaveTransactionWithoutCategory`
- Rename `` `should not save transaction without payment method` `` to `shouldNotSaveTransactionWithoutPaymentMethod`
- Rename `` `should filter categories by transaction type` `` to `shouldFilterCategoriesByTransactionType`
- Rename `` `should start editing transaction` `` to `shouldStartEditingTransaction`

## Verification Plan

### Automated Tests
- Run all unit tests to ensure they still pass and are correctly recognized by the test runner.
  - `.\gradlew :app:testDebugUnitTest`
