package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

class CalculateExpenseUseCase {
    operator fun invoke(transactions: List<Transaction>): Double {
        return transactions
            .filter { transaction ->
                transaction.type == TransactionType.EXPENSE && (transaction.creditCardId == null || transaction.creditCardBillId != null)
            }
            .sumOf { it.amount }
    }
}
