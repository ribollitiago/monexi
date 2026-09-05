package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.PaymentMethod
import kotlinx.coroutines.flow.StateFlow

interface PaymentMethodRepository {
    val paymentMethods: StateFlow<List<PaymentMethod>>

    fun addPaymentMethod(paymentMethod: PaymentMethod)
    fun deletePaymentMethod(paymentMethod: PaymentMethod)
}