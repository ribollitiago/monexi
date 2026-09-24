package com.moduxi.monexi.data.repository

import com.moduxi.monexi.data.local.dao.TransactionDao
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val authRepository: AuthRepository
) : TransactionRepository {

    override val transactions: Flow<List<Transaction>> = combine(
        categoryRepository.categories,
        paymentMethodRepository.paymentMethods
    ) { categories, paymentMethods ->
        Pair(categories, paymentMethods)
    }.flatMapLatest { (categories, paymentMethods) ->
        val currentUserId = authRepository.currentUser?.uid ?: ""
        transactionDao.observeTransactionsByUser(currentUserId).map { list ->
            list.mapNotNull { it.toDomain(categories, paymentMethods) }
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        val entity = transactionDao.getTransactionById(id) ?: return null

        val categories = categoryRepository.categories.first()
        val paymentMethods = paymentMethodRepository.paymentMethods.first()

        return entity.toDomain(categories, paymentMethods)
    }
}