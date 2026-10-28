package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.creditcard.CreditCard
import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    val creditCards: Flow<List<CreditCard>>
    val allCreditCards: Flow<List<CreditCard>>
    val archivedCreditCards: Flow<List<CreditCard>>

    suspend fun addCreditCard(creditCard: CreditCard)
    suspend fun updateCreditCard(creditCard: CreditCard)
    suspend fun deleteCreditCard(creditCard: CreditCard)
    suspend fun getCreditCardById(id: String): CreditCard?
    suspend fun syncFromRemote(): Result<Unit>
}