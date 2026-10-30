package com.moduxi.monexi.presentation.settings.accounts

import com.moduxi.monexi.domain.model.account.Account

data class AccountWithBalance(
    val account: Account,
    val currentBalance: Double
)
data class BankAccountUiState (
    val accounts: List<AccountWithBalance> = emptyList(),
    val error: String? = null
)