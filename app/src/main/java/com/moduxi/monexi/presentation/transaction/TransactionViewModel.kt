package com.moduxi.monexi.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.data.repository.InMemoryCategoryRepository
import com.moduxi.monexi.data.repository.InMemoryPaymentMethodRepository
import com.moduxi.monexi.data.repository.InMemoryTransactionRepository
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TransactionViewModel(
    private val transactionRepository: TransactionRepository = InMemoryTransactionRepository,
    private val categoryRepository: CategoryRepository = InMemoryCategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository = InMemoryPaymentMethodRepository
    ) : ViewModel() {

    private val formState = MutableStateFlow(TransactionUiState())

    val uiState = combine(
        categoryRepository.categories,
        paymentMethodRepository.paymentMethods,
        formState
    ) { categories, paymentMethods, form ->
        form.copy(
            categories = categories,
            paymentMethods = paymentMethods,
            selectedCategory = form.selectedCategory ?: categories.firstOrNull(),
            selectedPaymentMethod = form.selectedPaymentMethod ?: paymentMethods.firstOrNull()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TransactionViewModel(
                    transactionRepository = InMemoryTransactionRepository,
                    categoryRepository = InMemoryCategoryRepository,
                    paymentMethodRepository = InMemoryPaymentMethodRepository
                )
            }
        }
    }

    fun onTypeChange(type: TransactionType) {
        formState.value = formState.value.copy(
            type = type
        )
    }

    fun onDescriptionChange(description: String) {
        formState.value = formState.value.copy(
            description = description
        )
    }

    fun onAmountChange(value: String) {
        formState.value = formState.value.copy(
            amountDigits = value
                .filter { it.isDigit() }
                .take(12)
        )
    }

    fun onDateChange(dateMillis: Long) {
        formState.value = formState.value.copy(
            dateMillis = dateMillis
        )
    }

    fun onCategoryChange(category: Category) {
        formState.value = formState.value.copy(
            selectedCategory = category
        )
    }

    fun onPaymentMethodChange(paymentMethod: PaymentMethod) {
        formState.value = formState.value.copy(
            selectedPaymentMethod = paymentMethod
        )
    }

    fun saveTransaction(): Boolean {
        val state = uiState.value

        val category = state.selectedCategory ?: return false
        val paymentMethod = state.selectedPaymentMethod ?: return false
        val amount = (state.amountDigits.toLongOrNull() ?: 0L) / 100.0

        if (state.description.isBlank()) {
            formState.value = state.copy(error = "Informe a descrição")
            return false
        }

        if (amount <= 0.0) {
            formState.value = state.copy(error = "Informe um valor válido")
            return false
        }

        val transaction = Transaction(
            id = System.currentTimeMillis(),
            title = state.description,
            amount = amount,
            type = state.type,
            category = category,
            paymentMethod = paymentMethod,
            date = state.dateMillis
        )

        transactionRepository.addTransaction(transaction)

        return true
    }
}