package com.moduxi.monexi.presentation.settings.accounts

import com.moduxi.monexi.domain.model.Account

data class BankAccountUiState (
    val accounts: List<Account> = emptyList(),
    val error: String? = null
)