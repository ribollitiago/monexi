package com.moduxi.monexi.presentation.settings.accounts.archived

import com.moduxi.monexi.domain.model.account.Account

data class ArchivedBankAccountUiState(
    val accountsArchived: List<Account> = emptyList(),
    val error: String? = null
)