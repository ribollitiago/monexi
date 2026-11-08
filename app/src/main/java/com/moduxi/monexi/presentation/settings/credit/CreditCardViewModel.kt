package com.moduxi.monexi.presentation.settings.credit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CreditCardViewModel(
    private val creditCardRepository: CreditCardRepository
): ViewModel() {

    val uiState: StateFlow<CreditCardUiState> = creditCardRepository.creditCards
        .map { creditCards ->
            CreditCardUiState(
                creditCards = creditCards
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreditCardUiState()
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                CreditCardViewModel(
                    application.creditCardRepository
                )
            }
        }
    }
}