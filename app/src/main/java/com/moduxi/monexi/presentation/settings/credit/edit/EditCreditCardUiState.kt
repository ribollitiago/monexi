package com.moduxi.monexi.presentation.settings.credit.edit

import com.moduxi.monexi.domain.model.creditcard.CreditCard

data class EditCreditCardUiState(
    val creditCardName: String = "",
    val limitDigits: String = "",
    val creditCardClosingDay: Int = 0,
    val creditCardDueDay: Int = 0,
    val selectedColorHex: String = "#1565C0",
    val selectedIconName: String = "CreditCard",
    val editingCreditCard: CreditCard? = null,
    val error: String? = null
)
