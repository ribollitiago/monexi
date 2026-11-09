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
import com.moduxi.monexi.domain.model.recurring.Frequency
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.RecurringTransactionRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import com.moduxi.monexi.domain.usecase.CalculateAccountBalanceUseCase
import com.moduxi.monexi.domain.usecase.GenerateRecurringTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val accountRepository: AccountRepository,
    private val creditCardRepository: CreditCardRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val formState = MutableStateFlow(TransactionUiState())

    val uiState = combine(
        categoryRepository.categories,
        paymentMethodRepository.paymentMethods,
        accountRepository.accounts,
        creditCardRepository.creditCards,
        transactionRepository.transactions,
        formState
    ) { flows: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val categories = flows[0] as List<Category>
        @Suppress("UNCHECKED_CAST")
        val paymentMethods = flows[1] as List<PaymentMethod>
        @Suppress("UNCHECKED_CAST")
        val accounts = flows[2] as List<Account>
        @Suppress("UNCHECKED_CAST")
        val creditCards = flows[3] as List<CreditCard>
        @Suppress("UNCHECKED_CAST")
        val transactions = flows[4] as List<Transaction>
        val form = flows[5] as TransactionUiState

        val filteredCategories = categories.filter { category ->
            category.type == form.type && !category.isArchived
        }

        val filteredPaymentMethods = if (form.type == TransactionType.INCOME) {
            paymentMethods.filter { pm ->
                val name = pm.name.lowercase()
                !name.contains("crédito") && !name.contains("debito") && !name.contains("débito") && !name.contains("cartão")
            }
        } else {
            paymentMethods
        }

        val selectedAcc = form.selectedAccount ?: accounts.firstOrNull()
        val destAccounts = accounts.filter { it.id != selectedAcc?.id }
        val selectedDestAcc = form.selectedDestinationAccount
            ?.takeIf { it.id != selectedAcc?.id }
            ?: destAccounts.firstOrNull()

        val calculateBalance = CalculateAccountBalanceUseCase()
        val sourceBalance = selectedAcc?.let { calculateBalance(it, transactions) }
        val destBalance = selectedDestAcc?.let { calculateBalance(it, transactions) }

        form.copy(
            categories = filteredCategories,
            paymentMethods = filteredPaymentMethods,
            accounts = accounts,
            creditCards = creditCards,
            selectedCategory = form.selectedCategory
                ?.takeIf { it.type == form.type }
                ?: filteredCategories.firstOrNull(),
            selectedPaymentMethod = form.selectedPaymentMethod
                ?.takeIf { filteredPaymentMethods.contains(it) }
                ?: filteredPaymentMethods.firstOrNull(),
            selectedAccount = selectedAcc,
            selectedDestinationAccount = selectedDestAcc,
            sourceAccountBalance = sourceBalance,
            destinationAccountBalance = destBalance,
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
            isTransfer = false,
            selectedCategory = null,
            error = null
        )
    }

    fun onIsTransferChange(isTransfer: Boolean) {
        formState.value = formState.value.copy(
            isTransfer = isTransfer,
            error = null
        )
    }

    fun onIsRecurringChange(isRecurring: Boolean) {
        formState.value = formState.value.copy(
            isRecurring = isRecurring,
            error = null
        )
    }

    fun onRecurringDayOfMonthChange(day: Int) {
        formState.value = formState.value.copy(
            recurringDayOfMonth = day,
            error = null
        )
    }

    fun onIsRecurringInfiniteChange(isInfinite: Boolean) {
        formState.value = formState.value.copy(
            isRecurringInfinite = isInfinite,
            error = null
        )
    }

    fun onRecurringEndDateChange(endDateMillis: Long?) {
        formState.value = formState.value.copy(
            recurringEndDateMillis = endDateMillis,
            error = null
        )
    }

    fun onDescriptionChange(description: String) {
        formState.value = formState.value.copy(
            description = description,
            error = null
        )
    }

    fun onAmountChange(value: String) {
        formState.value = formState.value.copy(
            amountDigits = value
                .filter { it.isDigit() }
                .take(12),
            error = null
        )
    }

    fun onDateChange(dateMillis: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val day = cal.get(Calendar.DAY_OF_MONTH)

        formState.value = formState.value.copy(
            dateMillis = dateMillis,
            recurringDayOfMonth = day,
            error = null
        )
    }

    fun onInstallmentsChange(installments: Int) {
        formState.value = formState.value.copy(
            totalInstallments = installments,
            error = null
        )
    }

    fun onCategoryChange(category: Category) {
        formState.value = formState.value.copy(
            selectedCategory = category,
            error = null
        )
    }

    fun onPaymentMethodChange(paymentMethod: PaymentMethod) {
        formState.value = formState.value.copy(
            selectedPaymentMethod = paymentMethod,
            error = null
        )
    }

    fun onAccountChange(account: Account) {
        formState.value = formState.value.copy(
            selectedAccount = account,
            selectedDestinationAccount = if (formState.value.selectedDestinationAccount?.id == account.id) null else formState.value.selectedDestinationAccount,
            error = null
        )
    }

    fun onDestinationAccountChange(account: Account) {
        formState.value = formState.value.copy(
            selectedDestinationAccount = account,
            error = null
        )
    }

    fun onCreditCardChange(creditCard: CreditCard) {
        formState.value = formState.value.copy(
            selectedCreditCard = creditCard,
            error = null
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
                isTransfer = false,
                isRecurring = transaction.recurringTransactionId != null,
                selectedCategory = transaction.category,
                selectedPaymentMethod = transaction.paymentMethod,
                selectedAccount = transaction.accountId?.let { id -> accountsList.firstOrNull { it.id == id } },
                selectedCreditCard = transaction.creditCardId?.let { id -> creditCardsList.firstOrNull { it.id == id } },
                totalInstallments = transaction.totalInstallments ?: 1,
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

    fun deleteInstallmentGroup(groupId: String) {
        viewModelScope.launch {
            transactionRepository.deleteInstallmentGroup(groupId)
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
        val amount = (state.amountDigits.toLongOrNull() ?: 0L) / 100.0
        val editingTransaction = state.editingTransaction
        val currentUserId = authRepository.currentUser?.uid ?: ""

        if (amount <= 0.0) {
            formState.value = formState.value.copy(
                error = "Informe um valor válido"
            )
            return
        }

        // Salvar Transferência Entre Contas
        if (state.isTransfer) {
            val fromAccount = state.selectedAccount
            val toAccount = state.selectedDestinationAccount

            if (fromAccount == null || toAccount == null) {
                formState.value = formState.value.copy(error = "Selecione as contas de origem e destino")
                return
            }

            if (fromAccount.id == toAccount.id) {
                formState.value = formState.value.copy(error = "A conta de origem e destino não podem ser iguais")
                return
            }

            viewModelScope.launch {
                val allTransactions = transactionRepository.transactions.first()
                val calculateBalance = CalculateAccountBalanceUseCase()
                val availableBalance = calculateBalance(fromAccount, allTransactions)

                if (amount > availableBalance) {
                    val formattedBalance = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(availableBalance)
                    formState.value = formState.value.copy(
                        error = "Não é possível transferir por falta de saldo na conta ${fromAccount.name} ($formattedBalance disponível)"
                    )
                    return@launch
                }

                val category = state.categories.firstOrNull { it.name.contains("Transferência", ignoreCase = true) }
                    ?: state.categories.firstOrNull()

                val paymentMethod = state.paymentMethods.firstOrNull { it.name.contains("Transferência", ignoreCase = true) || it.name.contains("Pix", ignoreCase = true) }
                    ?: state.paymentMethods.firstOrNull()

                if (category == null || paymentMethod == null) {
                    formState.value = formState.value.copy(error = "Categoria ou forma de pagamento indisponível")
                    return@launch
                }

                val groupId = UUID.randomUUID().toString()
                val extraDesc = if (state.description.isNotBlank()) " - ${state.description}" else ""

                val sourceTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    title = "Transferência para ${toAccount.name}$extraDesc",
                    amount = amount,
                    type = TransactionType.EXPENSE,
                    category = category,
                    paymentMethod = paymentMethod,
                    date = state.dateMillis,
                    updatedAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.PENDING_CREATE,
                    accountId = fromAccount.id,
                    installmentGroupId = groupId
                )

                val destTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    title = "Transferência de ${fromAccount.name}$extraDesc",
                    amount = amount,
                    type = TransactionType.INCOME,
                    category = category,
                    paymentMethod = paymentMethod,
                    date = state.dateMillis,
                    updatedAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.PENDING_CREATE,
                    accountId = toAccount.id,
                    installmentGroupId = groupId
                )

                transactionRepository.addTransactions(listOf(sourceTx, destTx))
                onSaved()
            }
            return
        }

        // Salvar Transação Comum (Despesa / Receita)
        val category = state.selectedCategory
        val paymentMethod = state.selectedPaymentMethod

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

        val isCreditCardPayment = paymentMethod.name.contains("Crédito", ignoreCase = true)
        val installmentsCount = if (isCreditCardPayment) state.totalInstallments else 1

        viewModelScope.launch {
            val allTransactions = transactionRepository.transactions.first()

            // Validações de Saldo / Limite para novas Despesas
            if (state.type == TransactionType.EXPENSE && editingTransaction == null) {
                if (isCreditCardPayment) {
                    val card = state.selectedCreditCard
                    if (card != null) {
                        val totalSpent = allTransactions
                            .filter { it.creditCardId == card.id && it.deletedAt == null && it.type == TransactionType.EXPENSE && it.creditCardBillId == null }
                            .sumOf { it.amount }
                        val totalPaid = allTransactions
                            .filter { it.creditCardId == card.id && it.deletedAt == null && it.creditCardBillId != null }
                            .sumOf { it.amount }
                        val outstandingDebt = (totalSpent - totalPaid).coerceAtLeast(0.0)
                        val availableLimit = card.limitAmount - outstandingDebt

                        if (amount > availableLimit) {
                            val formattedLimit = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                                .format(availableLimit.coerceAtLeast(0.0))
                            formState.value = formState.value.copy(
                                error = "Limite insuficiente no cartão ${card.name} ($formattedLimit disponível)"
                            )
                            return@launch
                        }
                    } else {
                        formState.value = formState.value.copy(error = "Selecione um cartão de crédito")
                        return@launch
                    }
                } else {
                    val account = state.selectedAccount
                    if (account != null) {
                        val calculateBalance = CalculateAccountBalanceUseCase()
                        val availableBalance = calculateBalance(account, allTransactions)

                        if (amount > availableBalance) {
                            val formattedBalance = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                                .format(availableBalance)
                            formState.value = formState.value.copy(
                                error = "Saldo insuficiente na conta ${account.name} ($formattedBalance disponível)"
                            )
                            return@launch
                        }
                    } else {
                        formState.value = formState.value.copy(error = "Selecione uma conta bancária")
                        return@launch
                    }
                }
            }

            // Se o usuário marcou para tornar RECORRENTE:
            if (state.isRecurring && editingTransaction == null) {
                val recurring = RecurringTransaction(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    title = state.description,
                    amount = amount,
                    type = state.type,
                    categoryId = category.id,
                    paymentMethodId = paymentMethod.id,
                    accountId = if (!isCreditCardPayment) state.selectedAccount?.id else null,
                    creditCardId = if (isCreditCardPayment) state.selectedCreditCard?.id else null,
                    dayOfMonth = state.recurringDayOfMonth,
                    frequency = Frequency.MONTHLY,
                    startDate = state.dateMillis,
                    endDate = if (state.isRecurringInfinite) null else state.recurringEndDateMillis,
                    isActive = true
                )
                recurringTransactionRepository.addRecurringTransaction(recurring)

                // Gera o lançamento do mês atual
                val generateUseCase = GenerateRecurringTransactionsUseCase(
                    recurringTransactionRepository = recurringTransactionRepository,
                    transactionRepository = transactionRepository,
                    categoryRepository = categoryRepository,
                    paymentMethodRepository = paymentMethodRepository
                )
                generateUseCase(currentUserId)
                onSaved()
                return@launch
            }

            if (installmentsCount > 1 && isCreditCardPayment) {
                val totalCents = Math.round(amount * 100)
                val baseInstallmentCents = totalCents / installmentsCount
                val remainderCents = totalCents % installmentsCount
                val groupId = UUID.randomUUID().toString()

                val installmentsList = (1..installmentsCount).map { i ->
                    val installmentAmount = if (i == 1) {
                        (baseInstallmentCents + remainderCents) / 100.0
                    } else {
                        baseInstallmentCents / 100.0
                    }
                    val installmentDate = addMonthsToDate(state.dateMillis, i - 1)
                    Transaction(
                        id = if (i == 1 && editingTransaction != null) editingTransaction.id else UUID.randomUUID().toString(),
                        userId = currentUserId,
                        title = "${state.description} ($i/$installmentsCount)",
                        amount = installmentAmount,
                        type = state.type,
                        category = category,
                        paymentMethod = paymentMethod,
                        date = installmentDate,
                        updatedAt = System.currentTimeMillis(),
                        deletedAt = editingTransaction?.deletedAt,
                        syncStatus = SyncStatus.PENDING_CREATE,
                        creditCardId = state.selectedCreditCard?.id,
                        installmentNumber = i,
                        totalInstallments = installmentsCount,
                        installmentGroupId = groupId
                    )
                }

                transactionRepository.addTransactions(installmentsList)
            } else {
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
                    syncStatus = if (editingTransaction == null) SyncStatus.PENDING_CREATE else SyncStatus.PENDING_UPDATE,
                    remoteId = editingTransaction?.remoteId,
                    accountId = if (!isCreditCardPayment) state.selectedAccount?.id else null,
                    creditCardId = if (isCreditCardPayment) state.selectedCreditCard?.id else null,
                    installmentNumber = if (isCreditCardPayment) 1 else null,
                    totalInstallments = if (isCreditCardPayment) 1 else null
                )

                if (editingTransaction == null) {
                    transactionRepository.addTransaction(transaction)
                } else {
                    transactionRepository.updateTransaction(transaction)
                }
            }
            onSaved()
        }
    }

    private fun addMonthsToDate(baseDateMillis: Long, monthsToAdd: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseDateMillis
            add(Calendar.MONTH, monthsToAdd)
        }
        return calendar.timeInMillis
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
                    recurringTransactionRepository = application.recurringTransactionRepository,
                    authRepository = application.authRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
