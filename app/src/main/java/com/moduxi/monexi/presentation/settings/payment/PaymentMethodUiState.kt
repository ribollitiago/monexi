package com.moduxi.monexi.presentation.settings.payment

import com.moduxi.monexi.domain.model.payment.PaymentMethod

data class PaymentMethodUiState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val error: String? = null
)
