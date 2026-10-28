package com.moduxi.monexi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.dao.AccountDao
import com.moduxi.monexi.data.local.entity.AccountEntity
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RoomAccountRepository(
    private val accountDao: AccountDao,
    private val authRepository: AuthRepository
) : AccountRepository {

    override val accounts: Flow<List<Account>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return accountDao.observeAccountsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override val allAccounts: Flow<List<Account>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return accountDao.observeAllAccountsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override val archivedAccounts: Flow<List<Account>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return accountDao.observeArchivedAccountsByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override suspend fun addAccount(account: Account) {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        if (currentUserId.isEmpty()) {
            android.util.Log.e("FirestoreSync", "Erro ao adicionar conta: Usuário não logado!")
            return
        }

        val accountId = if (account.id == "0" || account.id.isEmpty()) {
            java.util.UUID.randomUUID().toString()
        } else {
            account.id
        }

        val entity = account.copy(id = accountId, userId = currentUserId).toEntity()

        accountDao.insertAccount(entity)

        val accountData = hashMapOf(
            "id" to accountId,
            "userId" to currentUserId,
            "name" to account.name,
            "initialBalance" to account.initialBalance,
            "colorHex" to account.colorHex,
            "iconName" to account.iconName,
            "isArchived" to account.isArchived
        )

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("accounts")
                .document(accountId)
                .set(accountData)
                .await()

            android.util.Log.d("FirestoreSync", "Conta $accountId salva no Firestore com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("FirestoreSync", "Erro ao salvar conta no Firestore: ${e.message}", e)
        }
    }

    override suspend fun updateAccount(account: Account) {
        val currentUserId = authRepository.currentUser?.uid ?: return

        accountDao.updateAccount(account.toEntity())

        val accountData = hashMapOf(
            "id" to account.id,
            "userId" to currentUserId,
            "name" to account.name,
            "initialBalance" to account.initialBalance,
            "colorHex" to account.colorHex,
            "iconName" to account.iconName,
            "isArchived" to account.isArchived
        )

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("accounts")
                .document(account.id)
                .set(accountData)
                .await()

            android.util.Log.d("FirestoreSync", "Conta ${account.id} atualizada no Firestore com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("FirestoreSync", "Erro ao atualizar conta no Firestore: ${e.message}", e)
        }
    }

    override suspend fun deleteAccount(account: Account) {
        val archivedAccount = account.copy(isArchived = true)

        accountDao.updateAccount(archivedAccount.toEntity())

        val currentUserId = authRepository.currentUser?.uid ?: return

        val accountData = hashMapOf(
            "id" to archivedAccount.id,
            "userId" to currentUserId,
            "name" to archivedAccount.name,
            "initialBalance" to archivedAccount.initialBalance,
            "colorHex" to archivedAccount.colorHex,
            "iconName" to archivedAccount.iconName,
            "isArchived" to true
        )

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("accounts")
                .document(archivedAccount.id)
                .set(accountData)
                .await()

            android.util.Log.d("FirestoreSync", "Conta ${archivedAccount.id} arquivada no Firestore com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("FirestoreSync", "Erro ao arquivar conta no Firestore: ${e.message}", e)
        }
    }

    override suspend fun getAccountById(id: String): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    override suspend fun syncFromRemote(): Result<Unit> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val currentUserId = authRepository.currentUser?.uid
            ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("accounts")
                .get()
                .await()

            val remoteAccounts = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val accountId = doc.getString("id") ?: doc.id
                val initialBalance = (doc.get("initialBalance") as? Number)?.toDouble() ?: 0.0
                val colorHex = doc.getString("colorHex") ?: "#1B8A5A"
                val iconName = doc.getString("iconName") ?: "AccountBalance"
                val isArchived = doc.getBoolean("isArchived") ?: false

                AccountEntity(
                    id = accountId,
                    userId = currentUserId,
                    name = name,
                    initialBalance = initialBalance,
                    colorHex = colorHex,
                    iconName = iconName,
                    isArchived = isArchived
                )
            }

            remoteAccounts.forEach { entity ->
                accountDao.insertAccount(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
