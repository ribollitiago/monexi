package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

class CalculateAccountBalanceUseCase {

    operator fun invoke(account: Account, transactions: List<Transaction>): Double {
        val accountTransactions = transactions.filter { it.accountId == account.id }

        val income = accountTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val expense = accountTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        return account.initialBalance + income - expense
    }
}