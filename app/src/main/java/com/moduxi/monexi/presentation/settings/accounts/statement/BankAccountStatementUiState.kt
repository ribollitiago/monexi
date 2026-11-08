package com.moduxi.monexi.presentation.settings.accounts.statement

import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.transaction.Transaction

data class BankAccountStatementUiState(
    val account: Account? = null,
    val currentBalance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
