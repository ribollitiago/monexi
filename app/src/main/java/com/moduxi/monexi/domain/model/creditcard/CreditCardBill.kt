package com.moduxi.monexi.domain.model.creditcard

import com.moduxi.monexi.domain.model.transaction.Transaction

data class CreditCardBill(
    val id: String,
    val creditCardId: String,
    val month: Int, // 1..12
    val year: Int,
    val periodStartDateMillis: Long = 0L,
    val closingDateMillis: Long,
    val dueDateMillis: Long,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val remainingBalance: Double = totalAmount,
    val creditAmount: Double = 0.0,
    val transactions: List<Transaction>,
    val isPaid: Boolean = false,
    val status: BillStatus = BillStatus.OPEN
)
