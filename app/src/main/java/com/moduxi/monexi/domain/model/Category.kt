package com.moduxi.monexi.domain.model

import androidx.room.PrimaryKey

data class Category (
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val type: TransactionType,
    val isDefault: Boolean = false
)