package com.moduxi.monexi.presentation.settings.accounts.archived

import com.moduxi.monexi.domain.model.Account

data class ArchivedBankAccountUiState(
    val accountsArchived: List<Account> = emptyList(),
    val error: String? = null
)