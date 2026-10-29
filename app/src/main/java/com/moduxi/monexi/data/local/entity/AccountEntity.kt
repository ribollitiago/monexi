package com.moduxi.monexi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moduxi.monexi.domain.model.account.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val initialBalance: Double = 0.0,
    val colorHex: String = "#1B8A5A",
    val iconName: String = "AccountBalance",
    val isArchived: Boolean = false,
    val type: AccountType = AccountType.CHECKING,
    val excludeFromTotal: Boolean = false
)
