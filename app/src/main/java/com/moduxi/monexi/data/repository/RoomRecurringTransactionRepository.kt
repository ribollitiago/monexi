package com.moduxi.monexi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.dao.RecurringTransactionDao
import com.moduxi.monexi.data.local.dao.TransactionDao
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RoomRecurringTransactionRepository(
    private val recurringTransactionDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao,
    private val authRepository: AuthRepository
) : RecurringTransactionRepository {

    override val activeRecurringTransactions: Flow<List<RecurringTransaction>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return recurringTransactionDao.observeActiveRecurringTransactionsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override val allRecurringTransactions: Flow<List<RecurringTransaction>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return recurringTransactionDao.observeAllRecurringTransactionsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val entity = recurringTransaction.copy(userId = currentUserId).toEntity()
        recurringTransactionDao.insertRecurringTransaction(entity)

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("recurring_transactions")
                .document(recurringTransaction.id)
                .set(entity)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val entity = recurringTransaction.copy(userId = currentUserId).toEntity()
        recurringTransactionDao.updateRecurringTransaction(entity)

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("recurring_transactions")
                .document(recurringTransaction.id)
                .set(entity)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteRecurringTransaction(
        recurringTransaction: RecurringTransaction,
        deletePastHistory: Boolean
    ) {
        val currentUserId = authRepository.currentUser?.uid ?: return

        // Desativa/Exclui a regra de recorrência
        recurringTransactionDao.deleteRecurringTransaction(recurringTransaction.toEntity())

        // Se o usuário optar por excluir o histórico passado gerado por essa recorrência:
        if (deletePastHistory) {
            val pastTransactions = transactionDao.getRecurringTransactionInPeriod(
                userId = currentUserId,
                recurringId = recurringTransaction.id,
                startDate = 0L,
                endDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
            )
            pastTransactions?.let { transactionDao.deleteTransaction(it) }
        }

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("recurring_transactions")
                .document(recurringTransaction.id)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getRecurringTransactionById(id: String): RecurringTransaction? {
        return recurringTransactionDao.getRecurringTransactionById(id)?.toDomain()
    }
}
