package com.moduxi.monexi.data.repository

import com.moduxi.monexi.data.local.dao.PaymentMethodDao
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPaymentMethodRepository (
    private val paymentMethodDao: PaymentMethodDao
) : PaymentMethodRepository {

    override val paymentMethods: Flow<List<PaymentMethod>> =
        paymentMethodDao.observePaymentMethods().map { paymentMethods ->
            paymentMethods.map { it.toDomain() }
        }

    override suspend fun addPaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.insertPaymentMethod(paymentMethod.toEntity())
    }

    override suspend fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        if (paymentMethod.isDefault) return

        paymentMethodDao.updatePaymentMethod(paymentMethod.toEntity())
    }

    override suspend fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        if (paymentMethod.isDefault) return

        paymentMethodDao.deletePaymentMethod(paymentMethod.toEntity())
    }
}