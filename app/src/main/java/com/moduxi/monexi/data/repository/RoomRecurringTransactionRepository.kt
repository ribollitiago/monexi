package com.moduxi.monexi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.dao.RecurringTransactionDao
import com.moduxi.monexi.data.local.dao.TransactionDao
import com.moduxi.monexi.data.local.entity.RecurringTransactionEntity
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.data.sync.RemoteStore
import com.moduxi.monexi.domain.model.recurring.Frequency
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RoomRecurringTransactionRepository(
    private val recurringTransactionDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao,
    private val authRepository: AuthRepository,
    private val remoteStore: RemoteStore
) : RecurringTransactionRepository {

    private fun RecurringTransactionEntity.toRemoteMap(userId: String): HashMap<String, Any?> = hashMapOf(
        "id" to id,
        "userId" to userId,
        "title" to title,
        "amount" to amount,
        "type" to type,
        "categoryId" to categoryId,
        "paymentMethodId" to paymentMethodId,
        "accountId" to accountId,
        "destinationAccountId" to destinationAccountId,
        "creditCardId" to creditCardId,
        "dayOfMonth" to dayOfMonth,
        "frequency" to frequency,
        "startDate" to startDate,
        "endDate" to endDate,
        "isActive" to isActive
    )

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

    /** Envia todas as recorrências locais do usuário (usado uma vez, para consertar dados antigos). */
    suspend fun pushAllLocal() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        recurringTransactionDao.observeAllRecurringTransactionsByUser(currentUserId).first().forEach { entity ->
            remoteStore.set(COLLECTION, entity.id, entity.toRemoteMap(currentUserId))
        }
    }

    override suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val entity = recurringTransaction.copy(userId = currentUserId).toEntity()
        recurringTransactionDao.insertRecurringTransaction(entity)

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set(COLLECTION, entity.id, entity.toRemoteMap(currentUserId))
    }

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val entity = recurringTransaction.copy(userId = currentUserId).toEntity()
        recurringTransactionDao.updateRecurringTransaction(entity)

        remoteStore.set(COLLECTION, entity.id, entity.toRemoteMap(currentUserId))
    }

    override suspend fun deleteRecurringTransaction(
        recurringTransaction: RecurringTransaction,
        deletePastHistory: Boolean
    ) {
        val currentUserId = authRepository.currentUser?.uid ?: return

        // Exclui a regra de recorrência
        recurringTransactionDao.deleteRecurringTransaction(recurringTransaction.toEntity())

        // Se o usuário optar por excluir o histórico gerado por essa recorrência:
        if (deletePastHistory) {
            val now = System.currentTimeMillis()
            val syncStatus = SyncStatus.PENDING_DELETE.name
            val generated = transactionDao.getTransactionsByRecurring(currentUserId, recurringTransaction.id)

            transactionDao.softDeleteTransactionsByRecurring(currentUserId, recurringTransaction.id, now, syncStatus)

            generated.forEach { entity ->
                val deleted = entity.copy(deletedAt = now, syncStatus = syncStatus)
                remoteStore.set("transactions", deleted.id, deleted.toRemoteMap(currentUserId))
            }
        }

        remoteStore.delete(COLLECTION, recurringTransaction.id)
    }

    override suspend fun getRecurringTransactionById(id: String): RecurringTransaction? {
        return recurringTransactionDao.getRecurringTransactionById(id)?.toDomain()
    }

    override suspend fun syncFromRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUserId = authRepository.currentUser?.uid
            ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection(COLLECTION)
                .get()
                .await()

            val remoteIds = snapshot.documents.map { it.getString("id") ?: it.id }.toSet()

            snapshot.documents.forEach { doc ->
                val title = doc.getString("title") ?: return@forEach
                val id = doc.getString("id") ?: doc.id

                // Alteração local ainda não enviada vence a versão da nuvem
                if (remoteStore.isPending(COLLECTION, id)) return@forEach

                val local = recurringTransactionDao.getRecurringTransactionById(id)

                // Ids antigos (aleatórios) das categorias/formas padrão viram os ids fixos
                val remoteCategoryId = doc.getString("categoryId")
                val remotePaymentMethodId = doc.getString("paymentMethodId")
                val categoryId = remoteCategoryId?.let { remoteStore.resolveLegacyId(it) }
                val paymentMethodId = remotePaymentMethodId?.let { remoteStore.resolveLegacyId(it) }

                val entity =
                    RecurringTransactionEntity(
                        id = id,
                        userId = currentUserId,
                        title = title,
                        amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0,
                        type = doc.getString("type") ?: "EXPENSE",
                        categoryId = categoryId ?: local?.categoryId ?: "",
                        // Documentos antigos foram enviados sem a forma de pagamento: mantém a local
                        paymentMethodId = paymentMethodId ?: local?.paymentMethodId,
                        accountId = doc.getString("accountId"),
                        destinationAccountId = doc.getString("destinationAccountId"),
                        creditCardId = doc.getString("creditCardId"),
                        dayOfMonth = (doc.get("dayOfMonth") as? Number)?.toInt() ?: 1,
                        frequency = doc.getString("frequency") ?: Frequency.MONTHLY.name,
                        startDate = (doc.get("startDate") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        endDate = (doc.get("endDate") as? Number)?.toLong(),
                        // Documentos antigos gravaram este campo como "active"
                        isActive = doc.getBoolean("isActive") ?: doc.getBoolean("active") ?: true
                    )

                recurringTransactionDao.insertRecurringTransaction(entity)

                // Devolve para a nuvem o documento já com os ids fixos
                if (categoryId != remoteCategoryId || paymentMethodId != remotePaymentMethodId) {
                    remoteStore.set(COLLECTION, entity.id, entity.toRemoteMap(currentUserId))
                }
            }

            // Recorrência cancelada em outro aparelho. Só vale com resposta do servidor (o cache
            // offline pode estar incompleto) e depois que os dados locais antigos já foram enviados.
            if (!snapshot.metadata.isFromCache && remoteStore.isFullPushDone()) {
                recurringTransactionDao.observeAllRecurringTransactionsByUser(currentUserId).first()
                    .filter { it.id !in remoteIds && !remoteStore.isPending(COLLECTION, it.id) }
                    .forEach { recurringTransactionDao.deleteRecurringTransaction(it) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val COLLECTION = "recurring_transactions"
    }
}
