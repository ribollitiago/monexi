package com.moduxi.monexi.domain.model.creditcard

data class CreditCardBill(
    val id: String,
    val creditCardId: String,
    val month: Int,
    val year: Int,
    val isPaid: Boolean,
    val paidAt: Long?,
    val paidFromAccountId: String?
)