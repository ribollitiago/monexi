package com.moduxi.monexi.domain.model.creditcard

import com.moduxi.monexi.domain.model.transaction.Transaction

data class CreditCardBill(
    val id: String,
    val creditCardId: String,
    val month: Int, // 1..12
    val year: Int,
    val closingDateMillis: Long,
    val dueDateMillis: Long,
    val totalAmount: Double,
    val transactions: List<Transaction>,
    val isPaid: Boolean = false
)
