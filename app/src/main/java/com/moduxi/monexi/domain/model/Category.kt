package com.moduxi.monexi.domain.model

data class Category (
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false
)