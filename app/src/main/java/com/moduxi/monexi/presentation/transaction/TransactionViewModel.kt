package com.moduxi.monexi.presentation.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val accountRepository: AccountRepository,
    private val creditCardRepository: CreditCardRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val formState = MutableStateFlow(TransactionUiState())

    val uiState = combine(
        categoryRepository.categories,
        paymentMethodRepository.paymentMethods,
        accountRepository.accounts,
        creditCardRepository.creditCards,
        formState
    ) { categories, paymentMethods, accounts, creditCards, form ->
        val filteredCategories = categories.filter { category ->
            category.type == form.type && !category.isArchived
        }

        form.copy(
            categories = filteredCategories,
            paymentMethods = paymentMethods,
            accounts = accounts,
            creditCards = creditCards,
            selectedCategory = form.selectedCategory
                ?.takeIf { it.type == form.type }
                ?: filteredCategories.firstOrNull(),
            selectedPaymentMethod = form.selectedPaymentMethod ?: paymentMethods.firstOrNull(),
            selectedAccount = form.selectedAccount ?: accounts.firstOrNull(),
            selectedCreditCard = form.selectedCreditCard ?: creditCards.firstOrNull()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionUiState()
    )

    init {
        val transactionId = savedStateHandle.get<String>("id")
        if (!transactionId.isNullOrEmpty()) {
            loadTransaction(transactionId)
        }
    }

    fun onTypeChange(type: TransactionType) {
        formState.value = formState.value.copy(
            type = type,
            selectedCategory = null,
            error = null
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

    fun onAccountChange(account: Account) {
        formState.value = formState.value.copy(
            selectedAccount = account
        )
    }

    fun onCreditCardChange(creditCard: CreditCard) {
        formState.value = formState.value.copy(
            selectedCreditCard = creditCard
        )
    }

    fun startEditing(transaction: Transaction) {
        viewModelScope.launch {
            val accountsList = accountRepository.allAccounts.first()
            val creditCardsList = creditCardRepository.allCreditCards.first()

            formState.value = formState.value.copy(
                editingTransaction = transaction,
                description = transaction.title,
                amountDigits = (transaction.amount * 100).toLong().toString(),
                type = transaction.type,
                selectedCategory = transaction.category,
                selectedPaymentMethod = transaction.paymentMethod,
                selectedAccount = transaction.accountId?.let { id -> accountsList.firstOrNull { it.id == id } },
                selectedCreditCard = transaction.creditCardId?.let { id -> creditCardsList.firstOrNull { it.id == id } },
                dateMillis = transaction.date,
                error = null
            )
        }
    }

    fun cancelEditing() {
        formState.value = TransactionUiState()
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(
                transaction.copy(
                    deletedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.PENDING_DELETE
                )
            )
        }
    }

    fun loadTransaction(id: String) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(id)
            transaction?.let { startEditing(it) }
        }
    }

    fun saveTransaction(onSaved: () -> Unit) {
        val state = uiState.value

        val category = state.selectedCategory
        val paymentMethod = state.selectedPaymentMethod
        val amount = (state.amountDigits.toLongOrNull() ?: 0L) / 100.0
        val editingTransaction = state.editingTransaction
        val currentUserId = authRepository.currentUser?.uid ?: ""

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

        val isCreditCardPayment = paymentMethod.name.contains("Crédito", ignoreCase = true)

        val transaction = Transaction(
            id = editingTransaction?.id ?: UUID.randomUUID().toString(),
            userId = currentUserId,
            title = state.description,
            amount = amount,
            type = state.type,
            category = category,
            paymentMethod = paymentMethod,
            date = state.dateMillis,
            updatedAt = System.currentTimeMillis(),
            deletedAt = editingTransaction?.deletedAt,
            syncStatus = if (editingTransaction == null) {
                SyncStatus.PENDING_CREATE
            } else {
                SyncStatus.PENDING_UPDATE
            },
            remoteId = editingTransaction?.remoteId,
            accountId = if (!isCreditCardPayment) state.selectedAccount?.id else null,
            creditCardId = if (isCreditCardPayment) state.selectedCreditCard?.id else null
        )

        viewModelScope.launch {
            if (editingTransaction == null) {
                transactionRepository.addTransaction(transaction)
            } else {
                transactionRepository.updateTransaction(transaction)
            }
            onSaved()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)

                val savedStateHandle = createSavedStateHandle()
                TransactionViewModel(
                    transactionRepository = application.transactionRepository,
                    categoryRepository = application.categoryRepository,
                    paymentMethodRepository = application.paymentMethodRepository,
                    accountRepository = application.accountRepository,
                    creditCardRepository = application.creditCardRepository,
                    authRepository = application.authRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
