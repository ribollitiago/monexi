package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.Transaction

class CalculateBalanceUseCase(
    private val calculateIncomeUseCase: CalculateIncomeUseCase = CalculateIncomeUseCase(),
    private val calculateExpenseUseCase: CalculateExpenseUseCase = CalculateExpenseUseCase()
) {

    operator fun invoke(transactions: List<Transaction>): Double {
        val income = calculateIncomeUseCase(transactions)
        val expense = calculateExpenseUseCase(transactions)

        return income - expense
    }
}