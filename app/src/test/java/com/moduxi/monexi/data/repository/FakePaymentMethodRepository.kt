package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.map

class FakePaymentMethodRepository(
    initialPaymentMethods: List<PaymentMethod> = emptyList()
) : PaymentMethodRepository {

    private val paymentMethodsState = MutableStateFlow(initialPaymentMethods)

    override val paymentMethods: Flow<List<PaymentMethod>> = paymentMethodsState
    override val allPaymentMethods: Flow<List<PaymentMethod>> = paymentMethodsState
    override val archivedPaymentMethod: Flow<List<PaymentMethod>>
        get() = paymentMethodsState.map { list -> list.filter { it.isArchived } }

    override suspend fun addPaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodsState.value += paymentMethod.copy(
            id = if (paymentMethod.id == "0" || paymentMethod.id.isEmpty()) java.util.UUID.randomUUID().toString() else paymentMethod.id
        )
    }

    override suspend fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodsState.value = paymentMethodsState.value.map {
            if (it.id == paymentMethod.id) paymentMethod else it
        }
    }

    override suspend fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodsState.value = paymentMethodsState.value.filterNot {
            it.id == paymentMethod.id
        }
    }

    override suspend fun syncFromRemote(): Result<Unit> {
        return Result.success(Unit)
    }
}
