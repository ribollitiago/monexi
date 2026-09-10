package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTransactionRepository(
    initialTransaction: List<Transaction> = emptyList()
) : TransactionRepository {

    private val transactionState = MutableStateFlow(initialTransaction)

    override val transactions: Flow<List<Transaction>> = transactionState

    override suspend fun addTransaction(transaction: Transaction) {
        transactionState.value += transaction.copy(
            id = if (transaction.id == 0L) transactionState.value.size + 1L else transaction.id
        )
    }
}