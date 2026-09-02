package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType

class CalculateIncomeUseCase {
    operator fun invoke(transactions: List<Transaction>): Double {
        return transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }
}