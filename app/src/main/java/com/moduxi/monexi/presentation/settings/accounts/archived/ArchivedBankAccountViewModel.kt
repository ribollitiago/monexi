package com.moduxi.monexi.presentation.settings.accounts.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.Account
import com.moduxi.monexi.domain.repository.AccountRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchivedBankAccountViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    val uiState: StateFlow<ArchivedBankAccountUiState> = accountRepository.archivedAccounts
        .map { accounts ->
            ArchivedBankAccountUiState(
                accountsArchived = accounts
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArchivedBankAccountUiState()
        )

    fun unarchiveBankAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.updateAccount(account.copy(isArchived = false))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                ArchivedBankAccountViewModel(
                    application.accountRepository
                )
            }
        }
    }
}
