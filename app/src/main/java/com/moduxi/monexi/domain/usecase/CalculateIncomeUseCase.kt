package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

class CalculateIncomeUseCase {
    operator fun invoke(transactions: List<Transaction>): Double {
        return transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }
}