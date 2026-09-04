package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.Transaction
import kotlinx.coroutines.flow.StateFlow

interface TransactionRepository {
    val transactions: StateFlow<List<Transaction>>

    fun addTransaction(transaction: Transaction)
}