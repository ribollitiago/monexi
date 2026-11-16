package com.moduxi.monexi.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.dao.PaymentMethodDao
import com.moduxi.monexi.data.local.entity.PaymentMethodEntity
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.data.sync.RemoteStore
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class RoomPaymentMethodRepository (
    private val paymentMethodDao: PaymentMethodDao,
    private val authRepository: AuthRepository,
    private val remoteStore: RemoteStore
) : PaymentMethodRepository {

    /** Envia as formas de pagamento personalizadas do usuário (as padrão têm id fixo). */
    suspend fun pushAllLocal() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        paymentMethodDao.observeAllPaymentMethodsByUser(currentUserId).first()
            .filter { !it.isDefault && it.userId == currentUserId }
            .forEach { entity ->
                remoteStore.set(
                    "paymentMethods",
                    entity.id,
                    hashMapOf(
                        "id" to entity.id,
                        "userId" to currentUserId,
                        "name" to entity.name,
                        "isDefault" to false,
                        "isArchived" to entity.isArchived
                    )
                )
            }
    }

    override val paymentMethods: Flow<List<PaymentMethod>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return paymentMethodDao.observePaymentMethodsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override val allPaymentMethods: Flow<List<PaymentMethod>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return paymentMethodDao.observeAllPaymentMethodsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override val archivedPaymentMethod: Flow<List<PaymentMethod>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return paymentMethodDao.observeArchivedPaymentMethodsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override suspend fun addPaymentMethod(paymentMethod: PaymentMethod) {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        if (currentUserId.isEmpty()) {
            Log.e("FirestoreSync", "Erro ao adicionar método de pagamento: Usuário não logado!")
            return
        }

        val paymentMethodId = if (paymentMethod.id == "0" || paymentMethod.id.isEmpty()) {
            UUID.randomUUID().toString()
        } else {
            paymentMethod.id
        }

        val entity = paymentMethod.copy(id = paymentMethodId, userId = currentUserId).toEntity()

        paymentMethodDao.insertPaymentMethod(entity)

        val paymentMethodData = hashMapOf(
            "id" to paymentMethodId,
            "userId" to currentUserId,
            "name" to paymentMethod.name,
            "isDefault" to false,
            "isArchived" to paymentMethod.isArchived
        )

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set("paymentMethods", paymentMethodId, paymentMethodData)
    }

    override suspend fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        if (paymentMethod.isDefault) return

        val currentUserId = authRepository.currentUser?.uid ?: return

        paymentMethodDao.updatePaymentMethod(paymentMethod.toEntity())

        val paymentMethodData = hashMapOf(
            "id" to paymentMethod.id,
            "userId" to paymentMethod.userId,
            "name" to paymentMethod.name,
            "isDefault" to false,
            "isArchived" to paymentMethod.isArchived
        )

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set("paymentMethods", paymentMethod.id.toString(), paymentMethodData)
    }

    override suspend fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        if (paymentMethod.isDefault) return

        val archivedPaymentMethod = paymentMethod.copy(isArchived = true)

        paymentMethodDao.updatePaymentMethod(archivedPaymentMethod.toEntity())

        val currentUserId = authRepository.currentUser?.uid ?: return

        val paymentMethodData = hashMapOf(
            "id" to archivedPaymentMethod.id,
            "userId" to archivedPaymentMethod.userId,
            "name" to archivedPaymentMethod.name,
            "isDefault" to archivedPaymentMethod.isDefault,
            "isArchived" to archivedPaymentMethod.isArchived
        )

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set("paymentMethods", archivedPaymentMethod.id, paymentMethodData)
    }

    override suspend fun getPaymentMethodById(id: String): PaymentMethod? {
        return paymentMethodDao.getPaymentMethodById(id)?.toDomain()
    }

    override suspend fun syncFromRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUserId = authRepository.currentUser?.uid
            ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("paymentMethods")
                .get()
                .await()

            val remotePaymentMethods = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val isDefault = doc.getBoolean("isDefault") ?: false
                val categoryId = doc.getString("id") ?: doc.id
                val isArchived = doc.getBoolean("isArchived") ?: false

                PaymentMethodEntity(
                    id = categoryId,
                    userId = currentUserId,
                    name = name,
                    isDefault = isDefault,
                    isArchived = isArchived
                )
            }

            remotePaymentMethods.forEach { entity ->
                // Alteração local ainda não enviada vence a versão da nuvem
                if (remoteStore.isPending("paymentMethods", entity.id)) return@forEach
                paymentMethodDao.insertPaymentMethod(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}