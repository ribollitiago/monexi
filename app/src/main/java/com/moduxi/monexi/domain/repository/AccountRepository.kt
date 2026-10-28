package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.account.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    val accounts: Flow<List<Account>>

    val allAccounts: Flow<List<Account>>

    val archivedAccounts: Flow<List<Account>>

    suspend fun addAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(account: Account)
    suspend fun getAccountById(id: String): Account?
    suspend fun syncFromRemote(): Result<Unit>
}