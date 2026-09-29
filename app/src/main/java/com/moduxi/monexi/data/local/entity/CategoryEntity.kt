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
    val isDefault: Boolean
)