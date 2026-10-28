package com.moduxi.monexi.domain.model.account

import java.util.UUID

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val initialBalance: Double = 0.0,
    val colorHex: String = "#1B8A5A",
    val iconName: String = "AccountBalance",
    val isArchived: Boolean = false,

    val type: AccountType,
    val excludeFromTotal: Boolean = false
)
