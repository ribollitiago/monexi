package com.moduxi.monexi.domain.model

data class Category (
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val isDefault: Boolean = false
)