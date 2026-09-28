package com.moduxi.monexi.domain.model

data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val paymentMethod: PaymentMethod,
    val date: Long
)
