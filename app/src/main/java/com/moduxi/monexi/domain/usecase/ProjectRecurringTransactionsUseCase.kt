package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.model.transaction.Transaction
import java.util.Calendar

/**
 * Projeta (sem salvar no banco) os lançamentos recorrentes que ainda não foram
 * gerados para o mês informado, para que o usuário veja com antecedência o que
 * está agendado. O lançamento real continua sendo criado pelo
 * [GenerateRecurringTransactionsUseCase] quando o dia chega.
 */
class ProjectRecurringTransactionsUseCase {

    operator fun invoke(
        recurringTransactions: List<RecurringTransaction>,
        existingTransactions: List<Transaction>,
        categories: List<Category>,
        paymentMethods: List<PaymentMethod>,
        year: Int,
        month: Int, // Calendar.MONTH (0 = janeiro)
        nowMillis: Long = System.currentTimeMillis()
    ): List<Transaction> {
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val selectedMonthIndex = monthIndex(year, month)

        // Meses passados nunca recebem projeção: o que aconteceu já está no banco
        if (selectedMonthIndex < monthIndex(nowCal.get(Calendar.YEAR), nowCal.get(Calendar.MONTH))) {
            return emptyList()
        }

        val alreadyGeneratedIds = existingTransactions
            .filter { transaction ->
                val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
                transaction.recurringTransactionId != null &&
                        cal.get(Calendar.YEAR) == year &&
                        cal.get(Calendar.MONTH) == month
            }
            .mapNotNull { it.recurringTransactionId }
            .toSet()

        return recurringTransactions.mapNotNull { recurring ->
            if (!recurring.isActive || recurring.id in alreadyGeneratedIds) return@mapNotNull null

            val startCal = Calendar.getInstance().apply { timeInMillis = recurring.startDate }
            if (selectedMonthIndex < monthIndex(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH))) {
                return@mapNotNull null
            }

            val targetCal = Calendar.getInstance().apply {
                clear()
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.DAY_OF_MONTH, recurring.dayOfMonth.coerceIn(1, getActualMaximum(Calendar.DAY_OF_MONTH)))
                set(Calendar.HOUR_OF_DAY, 12)
            }
            val targetMillis = targetCal.timeInMillis

            val endDate = recurring.endDate
            if (endDate != null && targetMillis > endDate) return@mapNotNull null

            val category = categories.firstOrNull { it.id == recurring.categoryId }
                ?: categories.firstOrNull() ?: return@mapNotNull null
            val paymentMethod = recurring.resolvePaymentMethod(paymentMethods) ?: return@mapNotNull null

            Transaction(
                id = "$SCHEDULED_ID_PREFIX${recurring.id}_${year}_${month + 1}",
                userId = recurring.userId,
                title = recurring.title,
                amount = recurring.amount,
                type = recurring.type,
                category = category,
                paymentMethod = paymentMethod,
                date = targetMillis,
                syncStatus = SyncStatus.SYNCED,
                accountId = recurring.accountId,
                creditCardId = recurring.creditCardId,
                recurringTransactionId = recurring.id
            )
        }
    }

    private fun monthIndex(year: Int, month: Int): Int = year * 12 + month

    companion object {
        const val SCHEDULED_ID_PREFIX = "scheduled_"

        /** true quando o lançamento é só uma projeção (não existe no banco). */
        fun isProjection(transaction: Transaction): Boolean =
            transaction.id.startsWith(SCHEDULED_ID_PREFIX)
    }
}
