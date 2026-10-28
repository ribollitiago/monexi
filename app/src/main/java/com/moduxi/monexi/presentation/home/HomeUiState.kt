package com.moduxi.monexi.presentation.home

import com.moduxi.monexi.domain.model.transaction.Transaction

data class HomeUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val selectedMonthName: String = "",
    val transactions: List<Transaction> = emptyList()
)
