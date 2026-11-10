package com.moduxi.monexi.presentation.onboarding

import com.moduxi.monexi.domain.model.account.AccountType

data class BankDraft(
    val name: String = "Nubank",
    val balanceDigits: String = "",
    val colorHex: String = "#1565C0",
    val iconName: String = "AccountBalance",
    val type: AccountType = AccountType.CHECKING
)

data class CardDraft(
    val name: String = "",
    val limitDigits: String = "",
    val closingDay: String = "5",
    val dueDay: String = "12",
    val colorHex: String = "#1565C0",
    val iconName: String = "CreditCard"
) {
    val isEmpty: Boolean get() = name.isBlank() && limitDigits.isBlank()
}

data class CategoryDraft(
    val name: String,
    val colorHex: String = "#E53935",
    val iconName: String = "Category"
)

data class OnboardingUiState(
    val step: Int = 0, // 0: Bank, 1: Card, 2: Categories
    val bank: BankDraft = BankDraft(),
    val card: CardDraft = CardDraft(),
    val categories: List<CategoryDraft> = emptyList(),
    val newCategoryName: String = "",
    val newCategoryColor: String = "#E53935",
    val error: String? = null,
    val isSaving: Boolean = false
)
