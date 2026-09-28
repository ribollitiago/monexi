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
import kotlinx.coroutines.tasks.await
import android.util.Log

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

        val currentUserId = authRepository.currentUser?.uid ?: return
        if (currentUserId.isEmpty()) {
            Log.e("FirestoreSync", "Erro: Usuário não autenticado no Firebase!")
            return
        }

        val transactionData = hashMapOf(
            "id" to transaction.id,
            "title" to transaction.title,
            "amount" to transaction.amount,
            "type" to transaction.type.name,
            "categoryId" to transaction.category.id,
            "paymentMethodId" to transaction.paymentMethod.id,
            "date" to transaction.date
        )

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("transactions")
                .document(transaction.id)
                .set(transactionData)
                .await()

            Log.d("FirestoreSync", "Transação ${transaction.id} salva no Firestore com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FirestoreSync", "Erro ao salvar no Firestore: ${e.message}", e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())

        val currentUserId = authRepository.currentUser?.uid ?: return

        val transactionData = hashMapOf(
            "id" to transaction.id,
            "title" to transaction.title,
            "amount" to transaction.amount,
            "type" to transaction.type.name,
            "categoryId" to transaction.category.id,
            "paymentMethodId" to transaction.paymentMethod.id,
            "date" to transaction.date
        )

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("transactions")
                .document(transaction.id)
                .set(transactionData)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())

        val currentUserId = authRepository.currentUser?.uid ?: return

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("transactions")
                .document(transaction.id)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        val entity = transactionDao.getTransactionById(id) ?: return null

        val categories = categoryRepository.categories.first()
        val paymentMethods = paymentMethodRepository.paymentMethods.first()

        return entity.toDomain(categories, paymentMethods)
    }

    override suspend fun syncFromRemote(): Result<Unit> {
        val currentUserId = authRepository.currentUser?.uid
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val snapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("transactions")
                .get()
                .await()

            val remoteEntities = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val amount = doc.getDouble("amount") ?: 0.0
                val type = doc.getString("type") ?: "EXPENSE"
                val categoryId = doc.getLong("categoryId") ?: 1L
                val paymentMethodId = doc.getLong("paymentMethodId") ?: 1L
                val date = doc.getLong("date") ?: System.currentTimeMillis()
                val transactionId = doc.getString("id") ?: doc.id

                com.moduxi.monexi.data.local.entity.TransactionEntity(
                    id = transactionId,
                    userId = currentUserId,
                    title = title,
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    paymentMethodId = paymentMethodId,
                    date = date
                )
            }

            remoteEntities.forEach { entity ->
                transactionDao.insertTransaction(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearLocalData() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        transactionDao.clearTransactionsByUser(currentUserId)
    }
}