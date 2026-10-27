package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.Account
import com.moduxi.monexi.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAccountRepository(
    initialAccounts: List<Account> = emptyList()
) : AccountRepository {

    private val accountsState = MutableStateFlow(initialAccounts)

    override val accounts: Flow<List<Account>>
        get() = accountsState.map { list -> list.filter { !it.isArchived } }

    override val allAccounts: Flow<List<Account>> = accountsState

    override val archivedAccounts: Flow<List<Account>>
        get() = accountsState.map { list -> list.filter { it.isArchived } }

    override suspend fun addAccount(account: Account) {
        accountsState.value += account.copy(
            id = if (account.id == "0" || account.id.isEmpty()) java.util.UUID.randomUUID().toString() else account.id
        )
    }

    override suspend fun updateAccount(account: Account) {
        accountsState.value = accountsState.value.map {
            if (it.id == account.id) account else it
        }
    }

    override suspend fun deleteAccount(account: Account) {
        accountsState.value = accountsState.value.map {
            if (it.id == account.id) it.copy(isArchived = true) else it
        }
    }

    override suspend fun getAccountById(id: String): Account? {
        return accountsState.value.firstOrNull { it.id == id }
    }

    override suspend fun syncFromRemote(): Result<Unit> {
        return Result.success(Unit)
    }
}
