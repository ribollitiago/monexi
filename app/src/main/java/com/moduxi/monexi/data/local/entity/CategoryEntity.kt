package com.moduxi.monexi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val name: String,
    val type: String,
    val isDefault: Boolean
)