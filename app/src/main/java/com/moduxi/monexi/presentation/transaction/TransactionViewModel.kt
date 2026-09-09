package com.moduxi.monexi.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.viewModelScope
import com.moduxi.monexi.MonexiApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
    ) : ViewModel() {

    private val formState = MutableStateFlow(TransactionUiState())



    val uiState = combine(
        categoryRepository.categories,
        paymentMethodRepository.paymentMethods,
        formState
    ) { categories, paymentMethods, form ->
        val filteredCategories = categories.filter { category ->
            category.type == form.type
        }

        form.copy(
            categories = filteredCategories,
            paymentMethods = paymentMethods,
            selectedCategory = form.selectedCategory
                ?.takeIf { it.type == form.type }
                ?: filteredCategories.firstOrNull(),
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
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                TransactionViewModel(
                    transactionRepository = application.transactionRepository,
                    categoryRepository = application.categoryRepository,
                    paymentMethodRepository = application.paymentMethodRepository
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

    fun saveTransaction(onSaved: () -> Unit) {
        val state = uiState.value

        val category = state.selectedCategory
        val paymentMethod = state.selectedPaymentMethod
        val amount = (state.amountDigits.toLongOrNull() ?: 0L) / 100.0

        if (category == null) {
            formState.value = formState.value.copy(
                error = "Selecione uma categoria"
            )
            return
        }

        if (paymentMethod == null) {
            formState.value = formState.value.copy(
                error = "Selecione uma forma de pagamento"
            )
            return
        }

        if (state.description.isBlank()) {
            formState.value = formState.value.copy(
                error = "Informe a descrição"
            )
            return
        }

        if (amount <= 0.0) {
            formState.value = formState.value.copy(
                error = "Informe um valor válido"
            )
            return
        }

        val transaction = Transaction(
            id = 0,
            title = state.description,
            amount = amount,
            type = state.type,
            category = category,
            paymentMethod = paymentMethod,
            date = state.dateMillis
        )

        viewModelScope.launch {
            transactionRepository.addTransaction(transaction)
            onSaved()
        }
    }
}