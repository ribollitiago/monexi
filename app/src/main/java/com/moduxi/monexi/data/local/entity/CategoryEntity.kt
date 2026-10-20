package com.moduxi.monexi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity (
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val type: String,
    val isDefault: Boolean,
    val isArchived: Boolean = false,
    val colorHex: String = "#1B8A5A",
    val iconName: String = "Category"
)