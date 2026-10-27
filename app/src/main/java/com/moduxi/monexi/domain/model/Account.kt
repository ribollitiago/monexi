package com.moduxi.monexi.domain.model

data class Account(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val initialBalance: Double = 0.0,
    val colorHex: String = "#1B8A5A",
    val iconName: String = "AccountBalance",
    val isArchived: Boolean = false
)
