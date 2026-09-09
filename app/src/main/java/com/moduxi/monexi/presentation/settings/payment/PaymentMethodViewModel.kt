package com.moduxi.monexi.presentation.settings.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentMethodViewModel(
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val formState = MutableStateFlow(PaymentMethodUiState())

    val uiState = combine(
        paymentMethodRepository.paymentMethods,
        formState
    ) { paymentMethods, form ->
        form.copy(
            paymentMethods = paymentMethods
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PaymentMethodUiState()
    )

    fun onNewPaymentMethodNameChange(name: String) {
        formState.value = formState.value.copy(
            newPaymentMethodName = name,
            error = null
        )
    }

    fun addPaymentMethod() {
        val state = formState.value
        val name = state.newPaymentMethodName.trim()

        if (name.isBlank()) {
            formState.value = state.copy(error = "Informe o nome do método de pagamento")
            return
        }
        val alreadyExists = uiState.value.paymentMethods.any { paymentMethod ->
            paymentMethod.name.equals(name, ignoreCase = true)
        }

        if (alreadyExists) {
            formState.value = state.copy(error = "Método de pagamento já existe")
            return
        }

        viewModelScope.launch{
            paymentMethodRepository.addPaymentMethod(
                PaymentMethod(
                    id = 0,
                    name = name,
                    isDefault = false
                )
            )

            formState.value = state.copy(
                newPaymentMethodName = "",
                error = null
            )
        }
    }

    fun startEditing(paymentMethod: PaymentMethod) {
        if (paymentMethod.isDefault) return

        formState.value = formState.value.copy(
            editingPaymentMethod = paymentMethod,
            editingPaymentMethodName = paymentMethod.name,
            error = null
        )
    }

    fun onEditingPaymentMethodChange(name: String) {
        formState.value = formState.value.copy(
            editingPaymentMethodName = name,
            error = null
        )
    }

    fun saveEditing() {
        val state = formState.value
        val paymentMethod = state.editingPaymentMethod ?: return
        val name = state.editingPaymentMethodName.trim()

        if (name.isBlank()) {
            formState.value = state.copy(error = "Informe o nome do método de pagamento")
            return
        }

        val alreadyExists = uiState.value.paymentMethods.any { currentPaymentMethod ->
            currentPaymentMethod.id != paymentMethod.id &&
                    currentPaymentMethod.name.equals(name, ignoreCase = true)
        }

        if (alreadyExists) {
            formState.value = state.copy(error = "Método de pagamento já existe")
            return
        }

        viewModelScope.launch {
            paymentMethodRepository.updatePaymentMethod(
                paymentMethod.copy(name = name)
            )

            formState.value = state.copy(
                editingPaymentMethod = null,
                editingPaymentMethodName = "",
                error = null
            )
        }
    }

    fun cancelEditing() {
        formState.value = formState.value.copy(
            editingPaymentMethod = null,
            editingPaymentMethodName = "",
            error = null
        )
    }

    fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        if (paymentMethod.isDefault) return

        viewModelScope.launch {
            paymentMethodRepository.deletePaymentMethod(paymentMethod)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                PaymentMethodViewModel(application.paymentMethodRepository)
            }
        }
    }
}