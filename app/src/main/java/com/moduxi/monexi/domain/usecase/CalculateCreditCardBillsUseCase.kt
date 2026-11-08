package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.creditcard.CreditCardBill
import com.moduxi.monexi.domain.model.transaction.Transaction
import java.util.Calendar

class CalculateCreditCardBillsUseCase {

    operator fun invoke(creditCard: CreditCard, transactions: List<Transaction>): List<CreditCardBill> {
        val cardTransactions = transactions.filter { it.creditCardId == creditCard.id && it.deletedAt == null }
        if (cardTransactions.isEmpty()) return emptyList()

        val closingDay = creditCard.closingDay
        val dueDay = creditCard.dueDay

        val billMap = mutableMapOf<String, MutableList<Transaction>>()

        cardTransactions.forEach { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            var month = cal.get(Calendar.MONTH) + 1
            var year = cal.get(Calendar.YEAR)

            if (day > closingDay) {
                month += 1
                if (month > 12) {
                    month = 1
                    year += 1
                }
            }

            val billKey = "${year}_$month"
            billMap.getOrPut(billKey) { mutableListOf() }.add(tx)
        }

        return billMap.map { (key, txs) ->
            val parts = key.split("_")
            val year = parts[0].toInt()
            val month = parts[1].toInt()

            val closingCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 2)
                set(Calendar.DAY_OF_MONTH, closingDay.coerceAtMost(getActualMaximum(Calendar.DAY_OF_MONTH)))
            }
            if (month == 1) {
                closingCal.set(Calendar.YEAR, year - 1)
                closingCal.set(Calendar.MONTH, 11)
            }

            val dueCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, dueDay.coerceAtMost(getActualMaximum(Calendar.DAY_OF_MONTH)))
            }

            val totalAmount = txs.sumOf { it.amount }

            CreditCardBill(
                id = "${creditCard.id}_${year}_$month",
                creditCardId = creditCard.id,
                month = month,
                year = year,
                closingDateMillis = closingCal.timeInMillis,
                dueDateMillis = dueCal.timeInMillis,
                totalAmount = totalAmount,
                transactions = txs.sortedByDescending { it.date },
                isPaid = false
            )
        }.sortedWith(compareBy({ it.year }, { it.month }))
    }
}
