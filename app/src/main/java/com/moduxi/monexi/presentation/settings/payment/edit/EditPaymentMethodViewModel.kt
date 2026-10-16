package com.moduxi.monexi.presentation.settings.payment.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditPaymentMethodViewModel (
    private val paymentMethodRepository: PaymentMethodRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPaymentMethodUiState())
    val uiState: StateFlow<EditPaymentMethodUiState> = _uiState.asStateFlow()

    init {
        val paymentMethodId: String? = savedStateHandle.get<String>("id")
        if (!paymentMethodId.isNullOrEmpty()) {
            loadPaymentMethod(paymentMethodId)
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            paymentMethodName = name,
            error = null
        )
    }

    fun loadPaymentMethod(id: String) {
        viewModelScope.launch {
            val category = paymentMethodRepository.getPaymentMethodById(id)
            category?.let {
                _uiState.value = _uiState.value.copy(
                    editingPaymentMethod = it,
                    paymentMethodName = it.name,
                )
            }
        }
    }

    fun savePaymentMethod(onSaved: () -> Unit) {
        val state = _uiState.value
        val name = state.paymentMethodName.trim()

        if (name.isBlank()) {
            _uiState.value = state.copy(error = "Informe o nome do método")
            return
        }

        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            val editingPaymentMethod = state.editingPaymentMethod

            if (editingPaymentMethod == null) {
                // Criando nova categoria
                paymentMethodRepository.addPaymentMethod(
                    PaymentMethod(
                        id = "0",
                        userId = currentUserId,
                        name = name,
                        isDefault = false
                    )
                )
            } else {
                // Editando categoria existente
                paymentMethodRepository.updatePaymentMethod(
                    editingPaymentMethod.copy(
                        name = name
                    )
                )
            }
            onSaved()
        }
    }

    fun deletePaymentMethod(onDeleted: () -> Unit) {
        val paymentMethod = _uiState.value.editingPaymentMethod ?: return
        viewModelScope.launch {
            paymentMethodRepository.deletePaymentMethod(paymentMethod)
            onDeleted()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                val savedStateHandle = createSavedStateHandle()

                EditPaymentMethodViewModel(
                    paymentMethodRepository = application.paymentMethodRepository,
                    authRepository = application.authRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}