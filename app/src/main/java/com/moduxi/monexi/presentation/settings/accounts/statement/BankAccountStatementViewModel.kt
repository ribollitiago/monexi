package com.moduxi.monexi.presentation.settings.accounts.statement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import com.moduxi.monexi.domain.usecase.CalculateAccountBalanceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class BankAccountStatementViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: String = savedStateHandle.get<String>("accountId") ?: ""
    private val calculateBalanceUseCase = CalculateAccountBalanceUseCase()

    val uiState = combine(
        accountRepository.allAccounts,
        transactionRepository.getTransactionsByAccount(accountId)
    ) { accounts, transactions ->
        val account = accounts.firstOrNull { it.id == accountId }
        val balance = if (account != null) calculateBalanceUseCase(account, transactions) else 0.0

        BankAccountStatementUiState(
            account = account,
            currentBalance = balance,
            transactions = transactions,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BankAccountStatementUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                val savedStateHandle = createSavedStateHandle()
                BankAccountStatementViewModel(
                    accountRepository = application.accountRepository,
                    transactionRepository = application.transactionRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
