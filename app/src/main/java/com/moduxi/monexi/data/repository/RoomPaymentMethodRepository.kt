package com.moduxi.monexi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.dao.PaymentMethodDao
import com.moduxi.monexi.data.local.entity.CategoryEntity
import com.moduxi.monexi.data.local.entity.PaymentMethodEntity
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RoomPaymentMethodRepository (
    private val paymentMethodDao: PaymentMethodDao,
    private val authRepository: AuthRepository
) : PaymentMethodRepository {

    override val paymentMethods: Flow<List<PaymentMethod>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return paymentMethodDao.observePaymentMethodsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override suspend fun addPaymentMethod(paymentMethod: PaymentMethod) {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        if (currentUserId.isEmpty()) {
            android.util.Log.e("FirestoreSync", "Erro ao adicionar método de pagamento: Usuário não logado!")
            return
        }

        val paymentMethodId = if (paymentMethod.id == "0" || paymentMethod.id.isEmpty()) {
            java.util.UUID.randomUUID().toString()
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

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("paymentMethods")
                .document(paymentMethodId)
                .set(paymentMethodData)
                .await()

            android.util.Log.d("FirestoreSync", "Método de Pagamento $paymentMethodId salva no Firestore com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("FirestoreSync", "Erro ao salvar o Método de Pagamento no Firestore: ${e.message}", e)
        }
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

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("paymentMethods")
                .document(paymentMethod.id.toString())
                .set(paymentMethodData)
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("paymentMethods")
                .document(archivedPaymentMethod.id)
                .set(paymentMethodData)
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun syncFromRemote(): Result<Unit> {
        val currentUserId = authRepository.currentUser?.uid
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
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

                PaymentMethodEntity(
                    id = categoryId,
                    userId = currentUserId,
                    name = name,
                    isDefault = isDefault
                )
            }

            remotePaymentMethods.forEach { entity ->
                paymentMethodDao.insertPaymentMethod(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}