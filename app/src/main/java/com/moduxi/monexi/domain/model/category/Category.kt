package com.moduxi.monexi.domain.model.category

import androidx.room.PrimaryKey
import com.moduxi.monexi.domain.model.transaction.TransactionType
import java.util.UUID

data class Category (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val type: TransactionType,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val colorHex: String = "#1B8A5A",
    val iconName: String = "Category"
)