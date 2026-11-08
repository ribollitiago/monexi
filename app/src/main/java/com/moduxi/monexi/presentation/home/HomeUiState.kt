package com.moduxi.monexi.presentation.home

import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.presentation.settings.accounts.AccountWithBalance

data class HomeUiState(
    val netWorth: Double = 0.0,
    val benefitsTotal: Double = 0.0,
    val accounts: List<AccountWithBalance> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val selectedMonthName: String = "",
    val transactions: List<Transaction> = emptyList()
)
