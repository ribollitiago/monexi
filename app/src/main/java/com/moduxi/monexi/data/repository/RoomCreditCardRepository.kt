package com.moduxi.monexi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.dao.CreditCardDao
import com.moduxi.monexi.data.local.dao.TransactionDao
import com.moduxi.monexi.data.local.entity.CreditCardEntity
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RoomCreditCardRepository(
    private val creditCardDao: CreditCardDao,
    private val transactionDao: TransactionDao,
    private val authRepository: AuthRepository
) : CreditCardRepository {

    override val creditCards: Flow<List<CreditCard>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return creditCardDao.observeCreditCardsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override val allCreditCards: Flow<List<CreditCard>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return creditCardDao.observeAllCreditCardsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override val archivedCreditCards: Flow<List<CreditCard>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return creditCardDao.observeArchivedCreditCardsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override suspend fun addCreditCard(creditCard: CreditCard) {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        if (currentUserId.isEmpty()) {
            android.util.Log.e("FirestoreSync", "Erro ao adicionar cartão: Usuário não logado!")
            return
        }

        val creditCardId = if (creditCard.id == "0" || creditCard.id.isEmpty()) {
            java.util.UUID.randomUUID().toString()
        } else {
            creditCard.id
        }

        val entity = creditCard.copy(id = creditCardId, userId = currentUserId).toEntity()

        creditCardDao.insertCreditCard(entity)

        val cardData = hashMapOf(
            "id" to creditCardId,
            "userId" to currentUserId,
            "name" to creditCard.name,
            "limitAmount" to creditCard.limitAmount,
            "closingDay" to creditCard.closingDay,
            "dueDay" to creditCard.dueDay,
            "colorHex" to creditCard.colorHex,
            "iconName" to creditCard.iconName,
            "isArchived" to creditCard.isArchived
        )

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("creditCards")
                .document(creditCardId)
                .set(cardData)
                .await()

            android.util.Log.d("FirestoreSync", "Cartão $creditCardId salvo no Firestore com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("FirestoreSync", "Erro ao salvar cartão no Firestore: ${e.message}", e)
        }
    }

    override suspend fun updateCreditCard(creditCard: CreditCard) {
        val currentUserId = authRepository.currentUser?.uid ?: return

        creditCardDao.updateCreditCard(creditCard.toEntity())

        val cardData = hashMapOf(
            "id" to creditCard.id,
            "userId" to currentUserId,
            "name" to creditCard.name,
            "limitAmount" to creditCard.limitAmount,
            "closingDay" to creditCard.closingDay,
            "dueDay" to creditCard.dueDay,
            "colorHex" to creditCard.colorHex,
            "iconName" to creditCard.iconName,
            "isArchived" to creditCard.isArchived
        )

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("creditCards")
                .document(creditCard.id)
                .set(cardData)
                .await()

            android.util.Log.d("FirestoreSync", "Cartão ${creditCard.id} atualizado no Firestore com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("FirestoreSync", "Erro ao atualizar cartão no Firestore: ${e.message}", e)
        }
    }

    override suspend fun deleteCreditCard(creditCard: CreditCard) {
        val archivedCard = creditCard.copy(isArchived = true)

        creditCardDao.updateCreditCard(archivedCard.toEntity())

        val currentUserId = authRepository.currentUser?.uid ?: return

        val cardData = hashMapOf(
            "id" to archivedCard.id,
            "userId" to currentUserId,
            "name" to archivedCard.name,
            "limitAmount" to archivedCard.limitAmount,
            "closingDay" to archivedCard.closingDay,
            "dueDay" to archivedCard.dueDay,
            "colorHex" to archivedCard.colorHex,
            "iconName" to archivedCard.iconName,
            "isArchived" to true
        )

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("creditCards")
                .document(archivedCard.id)
                .set(cardData)
                .await()

            android.util.Log.d("FirestoreSync", "Cartão ${archivedCard.id} arquivado no Firestore com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("FirestoreSync", "Erro ao arquivar cartão no Firestore: ${e.message}", e)
        }
    }

    override suspend fun getCreditCardById(id: String): CreditCard? {
        return creditCardDao.getCreditCardById(id)?.toDomain()
    }

    override suspend fun syncFromRemote(): Result<Unit> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val currentUserId = authRepository.currentUser?.uid
            ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("creditCards")
                .get()
                .await()

            val remoteCards = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val cardId = doc.getString("id") ?: doc.id
                val limitAmount = (doc.get("limitAmount") as? Number)?.toDouble() ?: 0.0
                val closingDay = (doc.get("closingDay") as? Number)?.toInt() ?: 5
                val dueDay = (doc.get("dueDay") as? Number)?.toInt() ?: 12
                val colorHex = doc.getString("colorHex") ?: "#1565C0"
                val iconName = doc.getString("iconName") ?: "CreditCard"
                val isArchived = doc.getBoolean("isArchived") ?: false
                val linkedAccountId = doc.getString("linkedAccountId") ?: ""

                CreditCardEntity(
                    id = cardId,
                    userId = currentUserId,
                    name = name,
                    limitAmount = limitAmount,
                    closingDay = closingDay,
                    dueDay = dueDay,
                    colorHex = colorHex,
                    iconName = iconName,
                    isArchived = isArchived,
                    linkedAccountId = linkedAccountId
                )
            }

            remoteCards.forEach { entity ->
                creditCardDao.insertCreditCard(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
