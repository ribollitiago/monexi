package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import java.util.Calendar

class CalculateAccountBalanceUseCase {

    /**
     * Saldo da conta. Com [upTo], lançamentos com data posterior ao limite (agendados /
     * futuros) ficam de fora; sem ele, soma tudo (comportamento anterior).
     */
    operator fun invoke(account: Account, transactions: List<Transaction>, upTo: Long? = null): Double {
        val accountTransactions = transactions.filter {
            it.accountId == account.id && (upTo == null || it.date <= upTo)
        }

        val income = accountTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val expense = accountTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        return account.initialBalance + income - expense
    }

    companion object {
        /** Último instante de hoje: saldo "real" considera tudo até aqui. */
        fun endOfToday(nowMillis: Long = System.currentTimeMillis()): Long =
            Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

        /** Último instante do mês de [nowMillis]. */
        fun endOfMonth(nowMillis: Long = System.currentTimeMillis()): Long =
            Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
    }
}
