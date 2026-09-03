package com.moduxi.monexi.presentation.transaction

import androidx.lifecycle.ViewModel
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TransactionViewModel : ViewModel() {
    private val defaultCategories = listOf(
        Category(id = 1, name = "Alimentacao", isDefault = true),
        Category(id = 2, name = "Transporte", isDefault = true),
        Category(id = 3, name = "Casa", isDefault = true),
        Category(id = 4, name = "Trabalho", isDefault = true)
    )

    private val defaultPaymentMethods = listOf(
        PaymentMethod(id = 1, name = "Dinheiro", isDefault = true),
        PaymentMethod(id = 2, name = "Pix", isDefault = true),
        PaymentMethod(id = 3, name = "Cartao de debito", isDefault = true),
        PaymentMethod(id = 4, name = "Cartao de credito", isDefault = true)
    )

    private val _uiState = MutableStateFlow(
        TransactionUiState(
            categories = defaultCategories,
            paymentMethods = defaultPaymentMethods,
            selectedCategory = defaultCategories.firstOrNull(),
            selectedPaymentMethod = defaultPaymentMethods.firstOrNull()
        )
    )

    val uiState = _uiState.asStateFlow()

    fun onTypeChange(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            type = type
        )
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description
        )
    }

    fun onAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(
            amountDigits = value
                .filter { it.isDigit() }
                .take(12)
        )
    }

    fun onDateChange(dateMillis: Long) {
        _uiState.value = _uiState.value.copy(
            dateMillis = dateMillis
        )
    }

    fun onCategoryChange(category: Category) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category
        )
    }

    fun onPaymentMethodChange(paymentMethod: PaymentMethod) {
        _uiState.value = _uiState.value.copy(
            selectedPaymentMethod = paymentMethod
        )
    }

    fun createTransaction(): Transaction? {
        val state = _uiState.value

        val category = state.selectedCategory ?: return null
        val paymentMethod = state.selectedPaymentMethod ?: return null
        val amount = (state.amountDigits.toLongOrNull() ?: 0L) / 100.0

        if (state.description.isBlank()) {
            _uiState.value = state.copy(error = "Informe a descrição")
            return null
        }

        if (amount <= 0.0) {
            _uiState.value = state.copy(error = "Informe um valor válido")
            return null
        }

        return Transaction(
            id = System.currentTimeMillis(),
            title = state.description,
            amount = amount,
            type = state.type,
            category = category,
            paymentMethod = paymentMethod,
            date = state.dateMillis
        )
    }
}