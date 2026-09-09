package com.moduxi.monexi.data.repository

import com.moduxi.monexi.data.local.dao.TransactionDao
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : TransactionRepository {

    override val transactions: Flow<List<Transaction>> = combine(
        transactionDao.observeTransactions(),
        categoryRepository.categories,
        paymentMethodRepository.paymentMethods
    ) { transactions, categories, paymentMethods ->
        transactions.mapNotNull { transaction ->
            transaction.toDomain(
                categories = categories,
                paymentMethods = paymentMethods
            )
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }
}