package com.moduxi.monexi.presentation.transaction

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.TransactionType

data class TransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val description: String = "",
    val amountDigits: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val selectedCategory: Category? = null,
    val selectedPaymentMethod: PaymentMethod? = null,
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)