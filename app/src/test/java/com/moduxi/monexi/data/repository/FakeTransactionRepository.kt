package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTransactionRepository(
    initialTransactions: List<Transaction> = emptyList()
) : TransactionRepository {

    private val transactionsState = MutableStateFlow(initialTransactions)

    override val transactions: Flow<List<Transaction>> = transactionsState

    val currentTransactions: List<Transaction>
        get() = transactionsState.value

    override suspend fun addTransaction(transaction: Transaction) {
        transactionsState.value += transaction.copy(
            id = if (transaction.id.isEmpty()) java.util.UUID.randomUUID().toString() else transaction.id
        )
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionsState.value = transactionsState.value.map {
            if (it.id == transaction.id) transaction else it
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionsState.value = transactionsState.value.filterNot {
            it.id == transaction.id
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionsState.value.firstOrNull { it.id == id }
    }

    override suspend fun syncFromRemote(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun clearLocalData() {
        transactionsState.value = emptyList()
    }
}
