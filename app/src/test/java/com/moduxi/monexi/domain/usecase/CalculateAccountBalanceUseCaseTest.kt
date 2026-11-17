package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalculateAccountBalanceUseCaseTest {

    private val useCase = CalculateAccountBalanceUseCase()
    private val account = Account(id = "acc", name = "Conta", initialBalance = 100.0)
    private val category = Category(id = "1", name = "Teste", type = TransactionType.EXPENSE)
    private val paymentMethod = PaymentMethod(id = "1", name = "Pix")

    private val now = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 17, 12, 0, 0)
    }.timeInMillis

    private fun tx(id: String, type: TransactionType, amount: Double, date: Long) = Transaction(
        id = id,
        title = id,
        amount = amount,
        type = type,
        category = category,
        paymentMethod = paymentMethod,
        date = date,
        accountId = "acc"
    )

    private fun daysFromNow(days: Int) = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_MONTH, days)
    }.timeInMillis

    @Test
    fun withoutLimit_sumsEverything() {
        val txs = listOf(
            tx("salario", TransactionType.INCOME, 3000.0, daysFromNow(30)),
            tx("mercado", TransactionType.EXPENSE, 50.0, daysFromNow(-1))
        )

        assertEquals(3050.0, useCase(account, txs), 0.0)
    }

    @Test
    fun withLimit_ignoresFutureTransactions() {
        val txs = listOf(
            tx("salario-proximo-mes", TransactionType.INCOME, 3000.0, daysFromNow(30)),
            tx("aluguel-proximo-mes", TransactionType.EXPENSE, 1000.0, daysFromNow(31)),
            tx("mercado-ontem", TransactionType.EXPENSE, 50.0, daysFromNow(-1))
        )

        val result = useCase(account, txs, CalculateAccountBalanceUseCase.endOfToday(now))

        assertEquals(50.0, result, 0.0)
    }

    @Test
    fun withLimit_includesTransactionsLaterToday() {
        val txs = listOf(tx("hoje", TransactionType.INCOME, 200.0, now))

        val result = useCase(account, txs, CalculateAccountBalanceUseCase.endOfToday(now))

        assertEquals(300.0, result, 0.0)
    }

    @Test
    fun endOfMonth_isAfterEndOfToday_andInSameMonth() {
        val endOfToday = CalculateAccountBalanceUseCase.endOfToday(now)
        val endOfMonth = CalculateAccountBalanceUseCase.endOfMonth(now)
        val cal = Calendar.getInstance().apply { timeInMillis = endOfMonth }

        assertTrue(endOfMonth > endOfToday)
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH))
        assertEquals(30, cal.get(Calendar.DAY_OF_MONTH))
    }
}
