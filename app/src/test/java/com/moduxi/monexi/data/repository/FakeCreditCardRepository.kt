package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.CreditCard
import com.moduxi.monexi.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCreditCardRepository(
    initialCreditCards: List<CreditCard> = emptyList()
) : CreditCardRepository {

    private val creditCardsState = MutableStateFlow(initialCreditCards)

    override val creditCards: Flow<List<CreditCard>>
        get() = creditCardsState.map { list -> list.filter { !it.isArchived } }

    override val allCreditCards: Flow<List<CreditCard>> = creditCardsState

    override val archivedCreditCards: Flow<List<CreditCard>>
        get() = creditCardsState.map { list -> list.filter { it.isArchived } }

    override suspend fun addCreditCard(creditCard: CreditCard) {
        creditCardsState.value += creditCard.copy(
            id = if (creditCard.id == "0" || creditCard.id.isEmpty()) java.util.UUID.randomUUID().toString() else creditCard.id
        )
    }

    override suspend fun updateCreditCard(creditCard: CreditCard) {
        creditCardsState.value = creditCardsState.value.map {
            if (it.id == creditCard.id) creditCard else it
        }
    }

    override suspend fun deleteCreditCard(creditCard: CreditCard) {
        creditCardsState.value = creditCardsState.value.map {
            if (it.id == creditCard.id) it.copy(isArchived = true) else it
        }
    }

    override suspend fun getCreditCardById(id: String): CreditCard? {
        return creditCardsState.value.firstOrNull { it.id == id }
    }

    override suspend fun syncFromRemote(): Result<Unit> {
        return Result.success(Unit)
    }
}
