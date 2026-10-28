package com.moduxi.monexi.domain.model.creditcard

import java.util.UUID

data class CreditCard(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val limitAmount: Double = 0.0,
    val closingDay: Int = 5,   // Dia que fecha a fatura
    val dueDay: Int = 12,      // Dia que vence a fatura
    val colorHex: String = "#1565C0",
    val iconName: String = "CreditCard",
    val isArchived: Boolean = false,
    val linkedAccountId: String? = null
)
