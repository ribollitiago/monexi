package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.creditcard.BillStatus
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.creditcard.CreditCardBill
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar

class CalculateCreditCardBillsUseCase {

    data class BillCycle(
        val year: Int,
        val month: Int,
        val startMillis: Long,
        val closingMillis: Long,
        val dueMillis: Long
    )

    operator fun invoke(
        creditCard: CreditCard,
        transactions: List<Transaction>,
        referenceDateMillis: Long = System.currentTimeMillis()
    ): List<CreditCardBill> {
        val activeTransactions = transactions.filter {
            it.creditCardId == creditCard.id && it.deletedAt == null
        }

        // Transações de compra/despesa com cartão
        val purchaseTransactions = activeTransactions.filter {
            it.creditCardBillId == null && it.type == TransactionType.EXPENSE
        }

        // Transações de pagamento de fatura vinculadas a este cartão (creditCardBillId != null)
        val paymentTransactions = activeTransactions.filter {
            it.creditCardBillId != null
        }

        val refCal = Calendar.getInstance().apply { timeInMillis = referenceDateMillis }
        val refYear = refCal.get(Calendar.YEAR)
        val refMonth = refCal.get(Calendar.MONTH) + 1

        val cyclesMap = mutableMapOf<String, BillCycle>()

        // 1. Determina ciclo atual com base na data de referência
        val currentCycle = getCycleForTimestamp(creditCard, referenceDateMillis)
        cyclesMap["${currentCycle.year}_${currentCycle.month}"] = currentCycle

        // 2. Determina o ciclo de cada compra existente
        val purchasesByCycleKey = mutableMapOf<String, MutableList<Transaction>>()
        purchaseTransactions.forEach { tx ->
            val cycle = getCycleForTimestamp(creditCard, tx.date)
            val key = "${cycle.year}_${cycle.month}"
            cyclesMap[key] = cycle
            purchasesByCycleKey.getOrPut(key) { mutableListOf() }.add(tx)
        }

        // 3. Inclui ciclos das faturas que possuem pagamentos registrados (mesmo sem compras novas)
        paymentTransactions.forEach { payTx ->
            val billId = payTx.creditCardBillId ?: return@forEach
            val parts = billId.split("_")
            if (parts.size >= 3) {
                val y = parts[parts.size - 2].toIntOrNull()
                val m = parts[parts.size - 1].toIntOrNull()
                if (y != null && m != null) {
                    val key = "${y}_$m"
                    if (!cyclesMap.containsKey(key)) {
                        cyclesMap[key] = calculateCycleForYearMonth(creditCard, y, m)
                    }
                }
            }
        }

        // 4. Monta cada CreditCardBill calculando com precisão monetária (centavos/BigDecimal)
        return cyclesMap.values.map { cycle ->
            val billId = "${creditCard.id}_${cycle.year}_${cycle.month}"
            val cyclePurchases = purchasesByCycleKey[billIdKey(cycle.year, cycle.month)] ?: emptyList()

            // Soma das despesas em BigDecimal
            val totalAmountCents = cyclePurchases.fold(BigDecimal.ZERO) { acc, tx ->
                acc.add(BigDecimal.valueOf(tx.amount))
            }.setScale(2, RoundingMode.HALF_UP)

            // Soma dos pagamentos aplicados a esta fatura
            val billPayments = paymentTransactions.filter { it.creditCardBillId == billId }
            val paidAmountCents = billPayments.fold(BigDecimal.ZERO) { acc, tx ->
                acc.add(BigDecimal.valueOf(tx.amount))
            }.setScale(2, RoundingMode.HALF_UP)

            // saldo_devido = total_lançamentos - pagamentos_aplicados
            val diff = totalAmountCents.subtract(paidAmountCents).setScale(2, RoundingMode.HALF_UP)

            val remainingBalance: Double
            val creditAmount: Double
            val isPaid: Boolean
            val isPartiallyPaid: Boolean

            if (diff <= BigDecimal.ZERO) {
                // Totalmente pago ou com crédito excedente
                remainingBalance = 0.0
                creditAmount = diff.abs().toDouble()
                isPaid = totalAmountCents > BigDecimal.ZERO || paidAmountCents > BigDecimal.ZERO
                isPartiallyPaid = false
            } else {
                remainingBalance = diff.toDouble()
                creditAmount = 0.0
                isPaid = false
                isPartiallyPaid = paidAmountCents > BigDecimal.ZERO
            }

            // Status da fatura
            val isCurrentCycle = cycle.year == currentCycle.year && cycle.month == currentCycle.month
            val isClosed = referenceDateMillis > cycle.closingMillis
            val isOverdue = referenceDateMillis > cycle.dueMillis && remainingBalance > 0.0

            val status = when {
                isPaid -> BillStatus.PAID
                isPartiallyPaid -> BillStatus.PARTIALLY_PAID
                isOverdue -> BillStatus.OVERDUE
                isClosed -> BillStatus.CLOSED
                isCurrentCycle -> BillStatus.OPEN
                cycle.year > currentCycle.year || (cycle.year == currentCycle.year && cycle.month > currentCycle.month) -> BillStatus.FUTURE
                else -> BillStatus.OPEN
            }

            CreditCardBill(
                id = billId,
                creditCardId = creditCard.id,
                month = cycle.month,
                year = cycle.year,
                periodStartDateMillis = cycle.startMillis,
                closingDateMillis = cycle.closingMillis,
                dueDateMillis = cycle.dueMillis,
                totalAmount = totalAmountCents.toDouble(),
                paidAmount = paidAmountCents.toDouble(),
                remainingBalance = remainingBalance,
                creditAmount = creditAmount,
                transactions = cyclePurchases.sortedByDescending { it.date },
                isPaid = isPaid,
                status = status
            )
        }.sortedWith(compareBy({ it.year }, { it.month }))
    }

    private fun billIdKey(year: Int, month: Int) = "${year}_$month"

    /**
     * Calcula o ciclo completo para um determinado ano e mês de fatura.
     * - Se dueDay > closingDay (ex: fecha dia 5, vence dia 12), fechamento e vencimento ocorrem no mesmo mês.
     * - Se dueDay <= closingDay (ex: fecha dia 25, vence dia 10), fechamento ocorre no mês anterior ao vencimento.
     */
    fun calculateCycleForYearMonth(creditCard: CreditCard, billYear: Int, billMonth: Int): BillCycle {
        val closingDay = creditCard.closingDay
        val dueDay = creditCard.dueDay

        val closingMonthOffset = if (dueDay > closingDay) 1 else 2

        val closingCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, billYear)
            set(Calendar.MONTH, billMonth - closingMonthOffset) // Calendar.MONTH é 0-based
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, closingDay.coerceAtMost(maxDay))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val prevClosingCal = Calendar.getInstance().apply {
            timeInMillis = closingCal.timeInMillis
            add(Calendar.MONTH, -1)
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, closingDay.coerceAtMost(maxDay))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val startMillis = prevClosingCal.timeInMillis + 1L

        val dueCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, billYear)
            set(Calendar.MONTH, billMonth - 1)
            val maxDueDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, dueDay.coerceAtMost(maxDueDay))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return BillCycle(
            year = billYear,
            month = billMonth,
            startMillis = startMillis,
            closingMillis = closingCal.timeInMillis,
            dueMillis = dueCal.timeInMillis
        )
    }

    /**
     * Atribui uma compra a um ciclo usando a data completa:
     * - Se a compra ocorreu até o fechamento da fatura do mês atual, pertence à fatura deste mês.
     * - Se a data de fechamento do mês atual já passou, a compra rola para a fatura do próximo mês.
     */
    fun getCycleForTimestamp(creditCard: CreditCard, timestamp: Long): BillCycle {
        val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val txYear = txCal.get(Calendar.YEAR)
        val txMonth = txCal.get(Calendar.MONTH) + 1 // 1..12

        val candidateMonth = if (creditCard.dueDay > creditCard.closingDay) txMonth else txMonth + 1
        var candidateYear = txYear
        var cMonth = candidateMonth
        if (cMonth > 12) {
            cMonth = 1
            candidateYear += 1
        }

        val candidateCycle = calculateCycleForYearMonth(creditCard, candidateYear, cMonth)

        if (timestamp < candidateCycle.startMillis) {
            var prevMonth = cMonth - 1
            var prevYear = candidateYear
            if (prevMonth < 1) {
                prevMonth = 12
                prevYear -= 1
            }
            return calculateCycleForYearMonth(creditCard, prevYear, prevMonth)
        }

        if (timestamp > candidateCycle.closingMillis) {
            var nextMonth = cMonth + 1
            var nextYear = candidateYear
            if (nextMonth > 12) {
                nextMonth = 1
                nextYear += 1
            }
            return calculateCycleForYearMonth(creditCard, nextYear, nextMonth)
        }

        return candidateCycle
    }
}
