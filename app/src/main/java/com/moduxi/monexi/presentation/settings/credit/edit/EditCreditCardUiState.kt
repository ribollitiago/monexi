package com.moduxi.monexi.presentation.settings.credit.edit

import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.creditcard.CreditCard

data class EditCreditCardUiState(
    val creditCardName: String = "",
    val limitDigits: String = "",
    val creditCardClosingDay: Int = 0,
    val creditCardDueDay: Int = 0,
    val selectedColorHex: String = "#1565C0",
    val selectedIconName: String = "CreditCard",
    val selectedAccount: Account? = null,
    val editingCreditCard: CreditCard? = null,
    val accounts: List<Account> = emptyList(),
    val error: String? = null
)
