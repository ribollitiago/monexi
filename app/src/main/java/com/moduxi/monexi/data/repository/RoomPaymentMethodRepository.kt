package com.moduxi.monexi.data.repository

import com.moduxi.monexi.data.local.dao.PaymentMethodDao
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPaymentMethodRepository (
    private val paymentMethodDao: PaymentMethodDao,
    private val authRepository: AuthRepository
) : PaymentMethodRepository {

    override val paymentMethods: Flow<List<PaymentMethod>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return paymentMethodDao.observePaymentMethodsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override suspend fun addPaymentMethod(paymentMethod: PaymentMethod) {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        val entity = paymentMethod.copy(userId = currentUserId).toEntity()
        paymentMethodDao.insertPaymentMethod(entity)
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