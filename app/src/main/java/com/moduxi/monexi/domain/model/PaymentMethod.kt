package com.moduxi.monexi.domain.model

data class PaymentMethod (
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false
)