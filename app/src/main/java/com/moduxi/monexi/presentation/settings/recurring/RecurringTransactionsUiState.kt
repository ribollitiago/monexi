package com.moduxi.monexi.presentation.settings.recurring

import com.moduxi.monexi.domain.model.recurring.RecurringTransaction

data class RecurringTransactionsUiState(
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
