package com.moduxi.monexi.presentation.settings.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import com.moduxi.monexi.domain.usecase.CalculateAccountBalanceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BankAccountViewModel (
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
): ViewModel() {

    private val calculateAccountBalanceUseCase = CalculateAccountBalanceUseCase()

    val uiState: StateFlow<BankAccountUiState> = combine(
        accountRepository.accounts,
        transactionRepository.transactions
    ) { accounts, transactions ->
        val accountsWithBalance = accounts.map { account ->
            AccountWithBalance(
                account = account,
                currentBalance = calculateAccountBalanceUseCase(account, transactions)
            )
        }
        BankAccountUiState(accounts = accountsWithBalance)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BankAccountUiState()
    )

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                BankAccountViewModel(
                    application.accountRepository,
                    transactionRepository = application.transactionRepository
                )
            }
        }
    }
}