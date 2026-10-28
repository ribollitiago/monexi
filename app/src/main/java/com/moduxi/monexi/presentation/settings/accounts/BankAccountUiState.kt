package com.moduxi.monexi.presentation.settings.accounts

import com.moduxi.monexi.domain.model.account.Account

data class BankAccountUiState (
    val accounts: List<Account> = emptyList(),
    val error: String? = null
)