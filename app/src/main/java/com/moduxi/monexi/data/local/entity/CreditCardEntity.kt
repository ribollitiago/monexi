package com.moduxi.monexi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("creditCards")
data class CreditCardEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val limitAmount: Double = 0.0,
    val closingDay: Int = 5,   // Dia que fecha a fatura
    val dueDay: Int = 12,      // Dia que vence a fatura
    val colorHex: String = "#1565C0",
    val iconName: String = "CreditCard",
    val isArchived: Boolean = false
)