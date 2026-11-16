package com.moduxi.monexi.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.dao.TransactionDao
import com.moduxi.monexi.data.local.entity.TransactionEntity
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.data.sync.RemoteStore
import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** Documento do lançamento no Firestore. */
internal fun TransactionEntity.toRemoteMap(userId: String): HashMap<String, Any?> = hashMapOf(
    "id" to id,
    "userId" to userId,
    "title" to title,
    "amount" to amount,
    "type" to type,
    "categoryId" to categoryId,
    "paymentMethodId" to paymentMethodId,
    "date" to date,
    "updatedAt" to updatedAt,
    "deletedAt" to deletedAt,
    "syncStatus" to syncStatus,
    "remoteId" to remoteId,
    "accountId" to accountId,
    "creditCardId" to creditCardId,
    "creditCardBillId" to creditCardBillId,
    "installmentNumber" to installmentNumber,
    "totalInstallments" to totalInstallments,
    "installmentGroupId" to installmentGroupId,
    "recurringTransactionId" to recurringTransactionId
)

class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val authRepository: AuthRepository,
    private val remoteStore: RemoteStore
) : TransactionRepository {

    /** Envia todos os lançamentos locais do usuário, inclusive os excluídos (usado uma vez, para consertar dados antigos). */
    suspend fun pushAllLocal() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        transactionDao.getAllTransactionsByUser(currentUserId).forEach { entity ->
            remoteStore.set("transactions", entity.id, entity.toRemoteMap(currentUserId))
        }
    }

    override val transactions: Flow<List<Transaction>> = combine(
        categoryRepository.allCategories,
        paymentMethodRepository.allPaymentMethods
    ) { categories, paymentMethods ->
        Pair(categories, paymentMethods)
    }.flatMapLatest { (categories, paymentMethods) ->
        val currentUserId = authRepository.currentUser?.uid ?: ""
        transactionDao.observeTransactionsByUser(currentUserId).map { list ->
            list.mapNotNull { it.toDomain(categories, paymentMethods) }
        }
    }

    override val deletedRecurringTransactions: Flow<List<Transaction>> = combine(
        categoryRepository.allCategories,
        paymentMethodRepository.allPaymentMethods
    ) { categories, paymentMethods ->
        Pair(categories, paymentMethods)
    }.flatMapLatest { (categories, paymentMethods) ->
        val currentUserId = authRepository.currentUser?.uid ?: ""
        transactionDao.observeDeletedRecurringTransactionsByUser(currentUserId).map { list ->
            list.mapNotNull { it.toDomain(categories, paymentMethods) }
        }
    }

    override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> = combine(
        categoryRepository.allCategories,
        paymentMethodRepository.allPaymentMethods
    ) { categories, paymentMethods ->
        Pair(categories, paymentMethods)
    }.flatMapLatest { (categories, paymentMethods) ->
        val currentUserId = authRepository.currentUser?.uid ?: ""
        transactionDao.observeTransactionsByAccount(currentUserId, accountId).map { list ->
            list.mapNotNull { it.toDomain(categories, paymentMethods) }
        }
    }

    override fun getTotalIncomeByAccount(accountId: String): Flow<Double> {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        return transactionDao.observeTotalIncomeByAccount(currentUserId, accountId)
    }

    override fun getTotalExpenseByAccount(accountId: String): Flow<Double> {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        return transactionDao.observeTotalExpenseByAccount(currentUserId, accountId)
    }

    override fun getTransactionsByCreditCard(creditCardId: String): Flow<List<Transaction>> = combine(
        categoryRepository.allCategories,
        paymentMethodRepository.allPaymentMethods
    ) { categories, paymentMethods ->
        Pair(categories, paymentMethods)
    }.flatMapLatest { (categories, paymentMethods) ->
        val currentUserId = authRepository.currentUser?.uid ?: ""
        transactionDao.observeTransactionsByCreditCard(currentUserId, creditCardId).map { list ->
            list.mapNotNull { it.toDomain(categories, paymentMethods) }
        }
    }

    override fun getTotalSpendByCreditCard(creditCardId: String): Flow<Double> {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        return transactionDao.observeTotalSpendByCreditCard(currentUserId, creditCardId)
    }

    override suspend fun addTransaction(transaction: Transaction) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        if (currentUserId.isEmpty()) {
            Log.e("FirestoreSync", "Erro: Usuário não autenticado no Firebase!")
            return
        }

        val entity = transaction.copy(userId = currentUserId).toEntity()
        transactionDao.insertTransaction(entity)

        val transactionData = hashMapOf(
            "id" to transaction.id,
            "userId" to currentUserId,
            "title" to transaction.title,
            "amount" to transaction.amount,
            "type" to transaction.type.name,
            "categoryId" to transaction.category.id,
            "paymentMethodId" to transaction.paymentMethod.id,
            "date" to transaction.date,
            "updatedAt" to transaction.updatedAt,
            "deletedAt" to transaction.deletedAt,
            "syncStatus" to transaction.syncStatus.name,
            "remoteId" to transaction.remoteId,
            "accountId" to transaction.accountId,
            "creditCardId" to transaction.creditCardId,
            "creditCardBillId" to transaction.creditCardBillId,
            "installmentNumber" to transaction.installmentNumber,
            "totalInstallments" to transaction.totalInstallments,
            "installmentGroupId" to transaction.installmentGroupId,
            "recurringTransactionId" to transaction.recurringTransactionId
        )

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set("transactions", transaction.id, transactionData)
    }

    override suspend fun addTransactions(transactions: List<Transaction>) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val entities = transactions.map { it.copy(userId = currentUserId).toEntity() }
        transactionDao.insertTransactions(entities)

        transactions.forEach { transaction ->
            addTransaction(transaction)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val updatedEntity = transaction.copy(userId = currentUserId, syncStatus = SyncStatus.PENDING_UPDATE).toEntity()
        transactionDao.updateTransaction(updatedEntity)

        val transactionData = hashMapOf(
            "id" to transaction.id,
            "userId" to currentUserId,
            "title" to transaction.title,
            "amount" to transaction.amount,
            "type" to transaction.type.name,
            "categoryId" to transaction.category.id,
            "paymentMethodId" to transaction.paymentMethod.id,
            "date" to transaction.date,
            "updatedAt" to transaction.updatedAt,
            "deletedAt" to transaction.deletedAt,
            "syncStatus" to SyncStatus.SYNCED.name,
            "remoteId" to transaction.remoteId,
            "accountId" to transaction.accountId,
            "creditCardId" to transaction.creditCardId,
            "creditCardBillId" to transaction.creditCardBillId,
            "installmentNumber" to transaction.installmentNumber,
            "totalInstallments" to transaction.totalInstallments,
            "installmentGroupId" to transaction.installmentGroupId,
            "recurringTransactionId" to transaction.recurringTransactionId
        )

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set("transactions", transaction.id, transactionData)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        transactionDao.updateTransaction(transaction.copy(userId = currentUserId).toEntity())

        val transactionData = hashMapOf(
            "id" to transaction.id,
            "userId" to currentUserId,
            "title" to transaction.title,
            "amount" to transaction.amount,
            "type" to transaction.type.name,
            "categoryId" to transaction.category.id,
            "paymentMethodId" to transaction.paymentMethod.id,
            "date" to transaction.date,
            "updatedAt" to transaction.updatedAt,
            "deletedAt" to transaction.deletedAt,
            "syncStatus" to transaction.syncStatus.name,
            "remoteId" to transaction.remoteId,
            "accountId" to transaction.accountId,
            "creditCardId" to transaction.creditCardId,
            "creditCardBillId" to transaction.creditCardBillId,
            "installmentNumber" to transaction.installmentNumber,
            "totalInstallments" to transaction.totalInstallments,
            "installmentGroupId" to transaction.installmentGroupId,
            "recurringTransactionId" to transaction.recurringTransactionId
        )

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set("transactions", transaction.id, transactionData)
    }

    override suspend fun deleteInstallmentGroup(groupId: String) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val now = System.currentTimeMillis()
        val syncStatus = SyncStatus.PENDING_DELETE.name

        val groupEntities = transactionDao.getInstallmentsByGroup(currentUserId, groupId)

        transactionDao.softDeleteTransactionsByGroup(currentUserId, groupId, now, syncStatus)

        groupEntities.forEach { entity ->
            val deleted = entity.copy(deletedAt = now, syncStatus = syncStatus)
            remoteStore.set("transactions", deleted.id, deleted.toRemoteMap(currentUserId))
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        val entity = transactionDao.getTransactionById(id) ?: return null

        val categories = categoryRepository.allCategories.first()
        val paymentMethods = paymentMethodRepository.allPaymentMethods.first()

        return entity.toDomain(categories, paymentMethods)
    }

    override suspend fun syncFromRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUserId = authRepository.currentUser?.uid
            ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("transactions")
                .get()
                .await()

            val remoteIds = snapshot.documents.map { it.id }.toSet()

            val localTransactions = transactionDao.observeTransactionsByUser(currentUserId).first()

            // Só remove o que sumiu da nuvem quando a resposta veio do servidor (o cache offline
            // pode estar incompleto) e não há envio pendente daquele lançamento
            if (!snapshot.metadata.isFromCache) {
                localTransactions.forEach { localEntity ->
                    if (localEntity.id !in remoteIds &&
                        localEntity.syncStatus == SyncStatus.SYNCED.name &&
                        !remoteStore.isPending("transactions", localEntity.id)
                    ) {
                        transactionDao.deleteTransaction(localEntity)
                    }
                }
            }

            val convertedIds = mutableSetOf<String>()

            val remoteEntities = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
                val type = doc.getString("type") ?: "EXPENSE"
                // Ids antigos (aleatórios) das categorias/formas padrão viram os ids fixos
                val remoteCategoryId = doc.getString("categoryId") ?: ""
                val remotePaymentMethodId = doc.getString("paymentMethodId") ?: ""
                val categoryId = remoteStore.resolveLegacyId(remoteCategoryId)
                val paymentMethodId = remoteStore.resolveLegacyId(remotePaymentMethodId)
                val date = (doc.get("date") as? Number)?.toLong() ?: System.currentTimeMillis()
                val transactionId = doc.getString("id") ?: doc.id
                if (categoryId != remoteCategoryId || paymentMethodId != remotePaymentMethodId) {
                    convertedIds.add(transactionId)
                }
                val updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                val deletedAt = (doc.get("deletedAt") as? Number)?.toLong()
                val syncStatus = doc.getString("syncStatus") ?: SyncStatus.SYNCED.name
                val remoteId = doc.getString("remoteId")

                val accountId = doc.getString("accountId")
                val creditCardId = doc.getString("creditCardId")
                val creditCardBillId = doc.getString("creditCardBillId")
                val installmentNumber = (doc.get("installmentNumber") as? Number)?.toInt()
                val totalInstallments = (doc.get("totalInstallments") as? Number)?.toInt()
                val installmentGroupId = doc.getString("installmentGroupId")
                val recurringTransactionId = doc.getString("recurringTransactionId")

                TransactionEntity(
                    id = transactionId,
                    userId = currentUserId,
                    title = title,
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    paymentMethodId = paymentMethodId,
                    date = date,
                    updatedAt = updatedAt,
                    deletedAt = deletedAt,
                    syncStatus = syncStatus,
                    remoteId = remoteId,
                    accountId = accountId,
                    creditCardId = creditCardId,
                    creditCardBillId = creditCardBillId,
                    installmentNumber = installmentNumber,
                    totalInstallments = totalInstallments,
                    installmentGroupId = installmentGroupId,
                    recurringTransactionId = recurringTransactionId
                )
            }

            remoteEntities.forEach { entity ->
                // Alteração local ainda não enviada vence a versão da nuvem
                if (remoteStore.isPending("transactions", entity.id)) return@forEach

                // Conflito: fica a edição mais recente
                val local = transactionDao.getTransactionById(entity.id)
                if (local != null && local.updatedAt > entity.updatedAt) return@forEach

                transactionDao.insertTransaction(entity)

                // Devolve para a nuvem o documento já com os ids fixos
                if (entity.id in convertedIds) {
                    remoteStore.set("transactions", entity.id, entity.toRemoteMap(currentUserId))
                }
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
