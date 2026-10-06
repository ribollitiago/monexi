package com.moduxi.monexi.presentation.settings.payment.archived

import com.moduxi.monexi.domain.model.PaymentMethod

data class ArchivedPaymentMethodUiState(
    val paymentMethodsArchived: List<PaymentMethod> = emptyList(),
    val error: String? = null
)