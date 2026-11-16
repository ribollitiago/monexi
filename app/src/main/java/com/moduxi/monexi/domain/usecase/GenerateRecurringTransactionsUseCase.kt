package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.RecurringTransactionRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.UUID

/** Id do lançamento de uma recorrência em um mês (month = Calendar.MONTH, 0 = janeiro). */
fun occurrenceId(recurringId: String, year: Int, month: Int): String =
    "rec_${recurringId}_${year}_${(month + 1).toString().padStart(2, '0')}"

class GenerateRecurringTransactionsUseCase(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) {

    suspend operator fun invoke(userId: String) {
        if (userId.isEmpty()) return

        val activeRecurring = recurringTransactionRepository.activeRecurringTransactions.first()
        if (activeRecurring.isEmpty()) return

        val categories = categoryRepository.categories.first()
        val paymentMethods = paymentMethodRepository.paymentMethods.first()

        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)

        val startOfMonthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endOfMonthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val startOfMonthMillis = startOfMonthCal.timeInMillis
        val endOfMonthMillis = endOfMonthCal.timeInMillis
        val nowMillis = System.currentTimeMillis()

        // Inclui as ocorrências excluídas pelo usuário, para não recriá-las
        val existingTransactions = transactionRepository.transactions.first() +
                transactionRepository.deletedRecurringTransactions.first()

        activeRecurring.forEach { recurring ->
            val endDate = recurring.endDate

            // Recorrência que só começa em um mês futuro ainda não gera lançamento
            val startCal = Calendar.getInstance().apply { timeInMillis = recurring.startDate }
            val startsAfterThisMonth = startCal.get(Calendar.YEAR) * 12 + startCal.get(Calendar.MONTH) >
                    currentYear * 12 + currentMonth
            if (startsAfterThisMonth) {
                return@forEach
            }

            val targetDay = recurring.dayOfMonth.coerceAtMost(startOfMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val targetCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, targetDay)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Data final inclusiva: só para de gerar quando a ocorrência passa dela
            if (endDate != null && targetCal.timeInMillis > endDate) {
                return@forEach
            }

            if (nowMillis >= targetCal.timeInMillis) {
                val existing = existingTransactions.firstOrNull {
                    it.recurringTransactionId == recurring.id &&
                            it.date >= startOfMonthMillis &&
                            it.date <= endOfMonthMillis
                }

                if (existing == null) {
                    val category = categories.firstOrNull { it.id == recurring.categoryId }
                        ?: categories.firstOrNull() ?: return@forEach

                    val paymentMethod = recurring.resolvePaymentMethod(paymentMethods) ?: return@forEach

                    val newTransaction = Transaction(
                        // Id determinístico: dois aparelhos gerando o mesmo mês criam o mesmo documento
                        id = occurrenceId(recurring.id, currentYear, currentMonth),
                        userId = userId,
                        title = recurring.title,
                        amount = recurring.amount,
                        type = recurring.type,
                        category = category,
                        paymentMethod = paymentMethod,
                        date = targetCal.timeInMillis,
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.PENDING_CREATE,
                        accountId = recurring.accountId,
                        creditCardId = recurring.creditCardId,
                        recurringTransactionId = recurring.id
                    )

                    transactionRepository.addTransaction(newTransaction)
                }
            }
        }
    }
}
