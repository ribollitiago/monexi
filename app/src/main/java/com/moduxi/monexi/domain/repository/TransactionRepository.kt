package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    val transactions: Flow<List<Transaction>>

    suspend fun addTransaction(transaction: Transaction)
}