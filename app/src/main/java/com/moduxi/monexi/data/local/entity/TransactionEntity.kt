package com.moduxi.monexi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity (
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val amount: Double,
    val type: String,
    val categoryId: Long,
    val paymentMethodId: Long,
    val date: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val syncStatus: String,
    val remoteId: String?
)