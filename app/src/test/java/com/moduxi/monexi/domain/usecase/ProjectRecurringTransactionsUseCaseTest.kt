package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ProjectRecurringTransactionsUseCaseTest {
    private val useCase = ProjectRecurringTransactionsUseCase()

    private val category = Category(id = "c1", name = "Assinaturas", type = TransactionType.EXPENSE)
    private val paymentMethod = PaymentMethod(id = "p1", name = "Pix")

    // "Hoje" = 16/11/2026
    private val now = millis(2026, Calendar.NOVEMBER, 16)

    private fun millis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 12, 0, 0)
        }.timeInMillis

    private fun recurring(
        dayOfMonth: Int = 10,
        startDate: Long = millis(2026, Calendar.NOVEMBER, 10),
        endDate: Long? = null,
        isActive: Boolean = true
    ) = RecurringTransaction(
        id = "r1",
        userId = "u1",
        title = "Netflix",
        amount = 55.9,
        type = TransactionType.EXPENSE,
        categoryId = "c1",
        paymentMethodId = "p1",
        accountId = "a1",
        dayOfMonth = dayOfMonth,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive
    )

    private fun project(
        recurring: RecurringTransaction,
        year: Int,
        month: Int,
        existing: List<Transaction> = emptyList()
    ) = useCase(
        recurringTransactions = listOf(recurring),
        existingTransactions = existing,
        categories = listOf(category),
        paymentMethods = listOf(paymentMethod),
        year = year,
        month = month,
        nowMillis = now
    )

    @Test
    fun `should project recurring expense for next month`() {
        val result = project(recurring(), 2026, Calendar.DECEMBER)

        assertEquals(1, result.size)
        val projected = result.first()
        assertEquals("Netflix", projected.title)
        assertEquals(55.9, projected.amount, 0.0)
        assertEquals("r1", projected.recurringTransactionId)
        assertEquals(millis(2026, Calendar.DECEMBER, 10), projected.date)
        assertTrue(ProjectRecurringTransactionsUseCase.isProjection(projected))
    }

    @Test
    fun `should not project when transaction was already generated in the month`() {
        val generated = Transaction(
            id = "t1",
            title = "Netflix",
            amount = 55.9,
            type = TransactionType.EXPENSE,
            category = category,
            paymentMethod = paymentMethod,
            date = millis(2026, Calendar.NOVEMBER, 10),
            recurringTransactionId = "r1"
        )

        val result = project(recurring(), 2026, Calendar.NOVEMBER, existing = listOf(generated))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should not project for past months`() {
        val result = project(
            recurring(startDate = millis(2026, Calendar.JANUARY, 10)),
            2026,
            Calendar.OCTOBER
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should not project after end date`() {
        val result = project(
            recurring(endDate = millis(2026, Calendar.DECEMBER, 31)),
            2027,
            Calendar.JANUARY
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should not project inactive recurring`() {
        val result = project(recurring(isActive = false), 2026, Calendar.DECEMBER)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should clamp day to last day of month`() {
        val result = project(recurring(dayOfMonth = 31), 2027, Calendar.FEBRUARY)

        assertEquals(millis(2027, Calendar.FEBRUARY, 28), result.first().date)
    }
}
