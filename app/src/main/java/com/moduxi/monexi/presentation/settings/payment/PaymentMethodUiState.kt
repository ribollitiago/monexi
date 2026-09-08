package com.moduxi.monexi.presentation.settings.payment

import com.moduxi.monexi.domain.model.PaymentMethod

data class PaymentMethodUiState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val newPaymentMethodName: String = "",
    val editingPaymentMethod: PaymentMethod? = null,
    val editingPaymentMethodName: String = "",
    val error: String? = null
)