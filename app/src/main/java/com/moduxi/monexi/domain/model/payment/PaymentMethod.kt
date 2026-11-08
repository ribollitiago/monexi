package com.moduxi.monexi.domain.model.payment

import androidx.room.PrimaryKey
import java.util.UUID

data class PaymentMethod (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)