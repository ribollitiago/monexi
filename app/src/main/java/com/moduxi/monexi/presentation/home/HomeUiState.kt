package com.moduxi.monexi.presentation.home

import com.moduxi.monexi.domain.model.Transaction

data class HomeUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val transactions: List<Transaction> = emptyList()
)
