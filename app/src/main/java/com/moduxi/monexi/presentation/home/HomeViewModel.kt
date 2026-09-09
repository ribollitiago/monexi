package com.moduxi.monexi.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.data.repository.InMemoryTransactionRepository
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.TransactionRepository
import com.moduxi.monexi.domain.usecase.CalculateBalanceUseCase
import com.moduxi.monexi.domain.usecase.CalculateExpenseUseCase
import com.moduxi.monexi.domain.usecase.CalculateIncomeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val transactionRepository: TransactionRepository
    ) : ViewModel() {
    private val calculateIncomeUseCase = CalculateIncomeUseCase()
    private val calculateExpenseUseCase = CalculateExpenseUseCase()
    private val calculateBalanceUseCase = CalculateBalanceUseCase(
        calculateIncomeUseCase = calculateIncomeUseCase,
        calculateExpenseUseCase = calculateExpenseUseCase
    )

    val uiState = transactionRepository.transactions
        .map { transactions ->
            HomeUiState(
                balance = calculateBalanceUseCase(transactions),
                totalIncome = calculateIncomeUseCase(transactions),
                totalExpense = calculateExpenseUseCase(transactions),
                transactions = transactions
            )

        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication
                HomeViewModel(application.transactionRepository)
            }
        }
    }
}
