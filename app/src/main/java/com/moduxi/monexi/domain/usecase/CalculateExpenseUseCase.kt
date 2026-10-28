package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

class CalculateExpenseUseCase {
    operator fun invoke(transactions: List<Transaction>): Double {
        return transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }
}