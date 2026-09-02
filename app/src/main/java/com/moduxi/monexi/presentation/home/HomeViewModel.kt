package com.moduxi.monexi.presentation.home

import androidx.lifecycle.ViewModel
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.usecase.CalculateBalanceUseCase
import com.moduxi.monexi.domain.usecase.CalculateExpenseUseCase
import com.moduxi.monexi.domain.usecase.CalculateIncomeUseCase

class HomeViewModel : ViewModel() {
    private val calculateIncomeUseCase = CalculateIncomeUseCase()
    private val calculateExpenseUseCase = CalculateExpenseUseCase()
    private val calculateBalanceUseCase = CalculateBalanceUseCase(
        calculateIncomeUseCase = calculateIncomeUseCase,
        calculateExpenseUseCase = calculateExpenseUseCase
    )
    private val transactions = listOf(
        Transaction(
            id = 1,
            title = "Salario",
            amount = 3200.0,
            type = TransactionType.INCOME,
            category = "Trabalho",
            date = System.currentTimeMillis()
        ),
        Transaction(
            id = 2,
            title = "Mercado",
            amount = 280.0,
            type = TransactionType.EXPENSE,
            category = "Alimentacao",
            date = System.currentTimeMillis()
        ),
        Transaction(
            id = 3,
            title = "Internet",
            amount = 120.0,
            type = TransactionType.EXPENSE,
            category = "Casa",
            date = System.currentTimeMillis()
        )
    )

    val uiState = HomeUiState(
        balance = calculateBalanceUseCase(transactions),
        totalIncome = calculateIncomeUseCase(transactions),
        totalExpense = calculateExpenseUseCase(transactions),
        transactions = transactions
    )
}
