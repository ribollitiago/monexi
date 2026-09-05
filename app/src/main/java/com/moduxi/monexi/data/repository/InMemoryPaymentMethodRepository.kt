package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object InMemoryPaymentMethodRepository : PaymentMethodRepository {

    private val _paymentMethods = MutableStateFlow(
        listOf(
            PaymentMethod(id = 1, name = "Pix", isDefault = true),
            PaymentMethod(id = 2, name = "Cartão de Crédio", isDefault = true),
            PaymentMethod(id = 3, name = "Cartão de Débito", isDefault = true),
            PaymentMethod(id = 4, name = "Boleto", isDefault = true),
            PaymentMethod(id = 5, name = "Dinheiro", isDefault = true),
        )
    )

    override val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    override fun addPaymentMethod(paymentMethod: PaymentMethod) {
        _paymentMethods.value += paymentMethod
    }

    override fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        _paymentMethods.value -= paymentMethod
    }
}