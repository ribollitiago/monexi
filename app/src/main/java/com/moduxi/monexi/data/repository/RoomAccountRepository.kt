package com.moduxi.monexi.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.DefaultData
import com.moduxi.monexi.data.local.dao.AccountDao
import com.moduxi.monexi.data.local.entity.AccountEntity
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.data.sync.RemoteStore
import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.account.AccountType
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class RoomAccountRepository(
    private val accountDao: AccountDao,
    private val authRepository: AuthRepository,
    private val remoteStore: RemoteStore
) : AccountRepository {

    private fun AccountEntity.toRemoteMap(userId: String): HashMap<String, Any?> = hashMapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "initialBalance" to initialBalance,
        "colorHex" to colorHex,
        "iconName" to iconName,
        "isArchived" to isArchived,
        "type" to type.name,
        "excludeFromTotal" to excludeFromTotal
    )

    /** Envia todas as contas locais do usuário (usado uma vez, para consertar dados antigos). */
    suspend fun pushAllLocal() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        accountDao.observeAllAccountsByUser(currentUserId).first().forEach { entity ->
            remoteStore.set("accounts", entity.id, entity.toRemoteMap(currentUserId))
        }
    }

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

    override suspend fun ensureDefaultAccount() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        if (currentUserId.isEmpty()) return
        val list = accountDao.observeAllAccountsByUser(currentUserId).first()
        if (list.isEmpty()) {
            // Id fixo por usuário e enviada para a nuvem: antes era só local, com id novo a cada instalação
            val defaultAccount = AccountEntity(
                id = DefaultData.walletAccountId(currentUserId),
                userId = currentUserId,
                name = "Carteira",
                initialBalance = 0.0,
                colorHex = "#43A047",
                iconName = "AttachMoney",
                type = AccountType.CASH
            )
            accountDao.insertAccount(defaultAccount)
            remoteStore.set("accounts", defaultAccount.id, defaultAccount.toRemoteMap(currentUserId))
        }
    }

    override suspend fun addAccount(account: Account) {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        if (currentUserId.isEmpty()) {
            Log.e("FirestoreSync", "Erro ao adicionar conta: Usuário não logado!")
            return
        }

        val accountId = if (account.id == "0" || account.id.isEmpty()) {
            UUID.randomUUID().toString()
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
            "isArchived" to account.isArchived,
            "type" to account.type.name,
            "excludeFromTotal" to account.excludeFromTotal
        )

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set("accounts", accountId, accountData)
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
            "isArchived" to account.isArchived,
            "type" to account.type.name,
            "excludeFromTotal" to account.excludeFromTotal
        )

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set("accounts", account.id, accountData)
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
            "isArchived" to true,
            "type" to archivedAccount.type.name,
            "excludeFromTotal" to archivedAccount.excludeFromTotal
        )

        // Envia sem esperar o servidor: já está salvo no Room e entra na fila de sincronização
        remoteStore.set("accounts", archivedAccount.id, accountData)
    }

    override suspend fun getAccountById(id: String): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    override suspend fun syncFromRemote(): Result<Unit> = withContext(Dispatchers.IO) {
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

                val typeString = doc.getString("type") ?: AccountType.CHECKING.name
                val type = try {
                    AccountType.valueOf(typeString)
                } catch (e: Exception) {
                    AccountType.CHECKING
                }

                // Documentos antigos não têm este campo: nesse caso mantém o valor local
                val excludeFromTotal = doc.getBoolean("excludeFromTotal")
                    ?: accountDao.getAccountById(accountId)?.excludeFromTotal
                    ?: false

                AccountEntity(
                    id = accountId,
                    userId = currentUserId,
                    name = name,
                    initialBalance = initialBalance,
                    colorHex = colorHex,
                    iconName = iconName,
                    isArchived = isArchived,
                    type = type,
                    excludeFromTotal = excludeFromTotal
                )
            }

            remoteAccounts.forEach { entity ->
                // Alteração local ainda não enviada vence a versão da nuvem
                if (remoteStore.isPending("accounts", entity.id)) return@forEach
                accountDao.insertAccount(entity)
            }

            // Garante que se ainda não houver nenhuma conta, cria a "Carteira"
            ensureDefaultAccount()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
