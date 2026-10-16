package com.moduxi.monexi.presentation.settings.payment.edit

import com.moduxi.monexi.domain.model.PaymentMethod

data class EditPaymentMethodUiState (
    val paymentMethodName: String = "",
    val editingPaymentMethod: PaymentMethod? = null,
    val error: String? = null
)