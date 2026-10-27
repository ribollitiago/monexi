package com.moduxi.monexi.presentation.settings.credit.edit

import com.moduxi.monexi.domain.model.CreditCard

data class EditCreditCardUiState(
    val creditCardName: String = "",
    val creditCardLimitAmount: Double = 0.0,
    val creditCardClosingDay: Int = 0,
    val creditCardDueDay: Int = 0,
    val selectedColorHex: String = "#1565C0",
    val selectedIconName: String = "CreditCard",
    val editingCreditCard: CreditCard? = null,
    val error: String? = null
)