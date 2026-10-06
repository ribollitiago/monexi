package com.moduxi.monexi.presentation.settings.payment.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchivedPaymentMethodViewModel(
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    val uiState: StateFlow<ArchivedPaymentMethodUiState> = paymentMethodRepository.archivedPaymentMethod
        .map { paymentMethods ->
            ArchivedPaymentMethodUiState(
                paymentMethodsArchived = paymentMethods
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArchivedPaymentMethodUiState()
        )

    fun unarchivedClick(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            paymentMethodRepository.updatePaymentMethod(paymentMethod.copy(isArchived = false))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                ArchivedPaymentMethodViewModel(
                    application.paymentMethodRepository
                )
            }
        }
    }
}