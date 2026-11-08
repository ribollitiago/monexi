package com.moduxi.monexi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val amount: Double,
    val type: String,
    val categoryId: String,
    val paymentMethodId: String? = null,
    val accountId: String? = null,
    val destinationAccountId: String? = null,
    val creditCardId: String? = null,
    val dayOfMonth: Int,
    val frequency: String,
    val startDate: Long,
    val endDate: Long? = null,
    val isActive: Boolean = true
)
