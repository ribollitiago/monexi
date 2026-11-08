package com.moduxi.monexi.presentation.settings.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecurringTransactionsViewModel(
    private val recurringTransactionRepository: RecurringTransactionRepository
) : ViewModel() {

    val uiState: StateFlow<RecurringTransactionsUiState> = recurringTransactionRepository.activeRecurringTransactions
        .map { list ->
            RecurringTransactionsUiState(
                recurringTransactions = list,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecurringTransactionsUiState()
        )

    fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction, deletePastHistory: Boolean) {
        viewModelScope.launch {
            recurringTransactionRepository.deleteRecurringTransaction(recurringTransaction, deletePastHistory)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                RecurringTransactionsViewModel(
                    recurringTransactionRepository = application.recurringTransactionRepository
                )
            }
        }
    }
}
