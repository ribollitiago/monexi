package com.moduxi.monexi.presentation.settings.accounts.edit

import com.moduxi.monexi.domain.model.account.Account

data class EditBankAccountUiState(
    val accountName: String = "",
    val balanceDigits: String = "",
    val selectedColorHex: String = "#1B8A5A",
    val selectedIconName: String = "AccountBalance",
    val editingBankAccount: Account? = null,
    val error: String? = null
)
