package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {
    val activeRecurringTransactions: Flow<List<RecurringTransaction>>
    val allRecurringTransactions: Flow<List<RecurringTransaction>>

    suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction, deletePastHistory: Boolean)
    suspend fun getRecurringTransactionById(id: String): RecurringTransaction?
}
