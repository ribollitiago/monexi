package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PaymentMethodRepository {
    val paymentMethods: Flow<List<PaymentMethod>>

    suspend fun addPaymentMethod(paymentMethod: PaymentMethod)
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod)
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod)
}