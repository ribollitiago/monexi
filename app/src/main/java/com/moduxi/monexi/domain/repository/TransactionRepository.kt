package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.transaction.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    val transactions: Flow<List<Transaction>>

    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>>
    fun getTotalIncomeByAccount(accountId: String): Flow<Double>
    fun getTotalExpenseByAccount(accountId: String): Flow<Double>

    fun getTransactionsByCreditCard(creditCardId: String): Flow<List<Transaction>>
    fun getTotalSpendByCreditCard(creditCardId: String): Flow<Double>

    suspend fun addTransaction(transaction: Transaction)
    suspend fun addTransactions(transactions: List<Transaction>)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun syncFromRemote(): Result<Unit>
    suspend fun clearLocalData()
}
