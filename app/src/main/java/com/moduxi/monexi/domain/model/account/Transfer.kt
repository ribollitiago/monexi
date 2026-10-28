package com.moduxi.monexi.domain.model.account

data class Transfer(
    val id: String,
    val userId: String,
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Double,
    val date: Long,
    val description: String
)
