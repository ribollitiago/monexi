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
import com.moduxi.monexi.domain.model.account.AccountType
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
import com.moduxi.monexi.domain.usecase.firstNonCredit
import com.moduxi.monexi.domain.usecase.isCompatibleWith
import com.moduxi.monexi.domain.usecase.occurrenceId
import com.moduxi.monexi.data.local.mapper.MISSING_CATEGORY_NAME
import com.moduxi.monexi.domain.usecase.isCredit
import com.moduxi.monexi.domain.usecase.isValidFor
import com.moduxi.monexi.domain.usecase.resolvePaymentMethod
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

    // Pernas (saída, entrada) da transferência em edição
    private var editingTransferLegs: Pair<Transaction, Transaction>? = null

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

        val hasCreditCard = creditCards.isNotEmpty()

        // Só é elegível a forma de pagamento que tem onde ser usada:
        // crédito precisa de cartão; as demais, de ao menos uma conta compatível
        val isEligiblePm: (PaymentMethod) -> Boolean = { pm ->
            if (pm.isCredit()) hasCreditCard else accounts.any { pm.isCompatibleWith(it.type) }
        }

        // Receita: quem manda é a conta. Todas as contas ficam disponíveis (para a pessoa
        // lançar saldo em carteira, vale, etc.) e a forma de pagamento se adapta à conta.
        // Despesa: quem manda é a forma de pagamento, que filtra as contas.
        val isIncomeForm = form.type == TransactionType.INCOME && !form.isTransfer
        val incomeAccount = form.selectedAccount?.takeIf { acc -> accounts.any { it.id == acc.id } }
            ?: accounts.firstOrNull()

        val paymentOptions = if (isIncomeForm && incomeAccount != null) {
            filteredPaymentMethods.filter { it.isCompatibleWith(incomeAccount.type) }
        } else {
            filteredPaymentMethods
        }

        val selectedPm = if (isIncomeForm) {
            form.selectedPaymentMethod?.takeIf { paymentOptions.contains(it) }
                ?: paymentOptions.firstNonCredit(incomeAccount?.type, TransactionType.INCOME)
        } else {
            form.selectedPaymentMethod
                ?.takeIf { filteredPaymentMethods.contains(it) && isEligiblePm(it) }
                ?: filteredPaymentMethods.firstOrNull { isEligiblePm(it) }
                ?: filteredPaymentMethods.firstOrNull()
        }

        // Contas que fazem sentido para a forma de pagamento (Dinheiro -> carteira; Pix/Débito -> banco...)
        val availableAccounts = if (isIncomeForm || selectedPm == null || selectedPm.isCredit()) {
            accounts
        } else {
            accounts.filter { selectedPm.isCompatibleWith(it.type) }
        }
        val selectedAccount = when {
            // Na transferência a forma de pagamento não se aplica: vale qualquer conta
            form.isTransfer -> selectedAcc
            isIncomeForm -> incomeAccount
            else -> form.selectedAccount?.takeIf { acc -> availableAccounts.any { it.id == acc.id } }
                ?: availableAccounts.firstOrNull()
        }

        form.copy(
            categories = filteredCategories,
            paymentMethods = paymentOptions,
            accounts = accounts,
            availableAccounts = availableAccounts,
            creditCards = creditCards,
            selectedCategory = form.selectedCategory
                ?.takeIf { it.type == form.type }
                ?: filteredCategories.firstOrNull(),
            selectedPaymentMethod = selectedPm,
            selectedAccount = selectedAccount,
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

        val recurringId = savedStateHandle.get<String>("recurringId")
        if (!recurringId.isNullOrEmpty()) {
            val occurrenceDate = savedStateHandle.get<Long>("occurrenceDate")?.takeIf { it > 0L }
            loadRecurring(recurringId, occurrenceDate)
        }
    }

    fun onTypeChange(type: TransactionType) {
        // Uma transferência em edição não pode virar despesa/receita
        if (editingTransferLegs != null) return
        formState.value = formState.value.copy(
            type = type,
            isTransfer = false,
            selectedCategory = null,
            error = null
        )
    }

    fun onIsTransferChange(isTransfer: Boolean) {
        // Recorrência ou lançamento já existente não pode virar transferência
        if (formState.value.editingRecurring != null || formState.value.editingTransaction != null) return
        formState.value = formState.value.copy(
            isTransfer = isTransfer,
            error = null
        )
    }

    fun onIsRecurringChange(isRecurring: Boolean) {
        if (formState.value.editingRecurring != null) return
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
            // meio-dia local do dia escolhido (mesmo horário dos lançamentos gerados), para a data final ser inclusiva
            recurringEndDateMillis = endDateMillis?.let { pickerDateToLocal(it, hour = 12, minute = 0) },
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
        // O DatePicker devolve meia-noite em UTC; no Brasil isso cairia no dia anterior
        val localDateMillis = pickerDateToLocal(dateMillis, hour = 12, minute = 0)
        val cal = Calendar.getInstance().apply { timeInMillis = localDateMillis }
        val day = cal.get(Calendar.DAY_OF_MONTH)

        formState.value = formState.value.copy(
            dateMillis = localDateMillis,
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
            val allTxs = transactionRepository.transactions.first()

            val isGroup = !transaction.installmentGroupId.isNullOrEmpty()
            val groupTxs = if (isGroup) {
                allTxs.filter { it.installmentGroupId == transaction.installmentGroupId && it.deletedAt == null }
            } else emptyList()

            // Transferência: duas pernas (saída + entrada) no mesmo grupo, sem número de parcela.
            // Sem isso ela abria como compra parcelada, com valor dobrado e sem as contas.
            val expenseLeg = groupTxs.firstOrNull { it.type == TransactionType.EXPENSE }
            val incomeLeg = groupTxs.firstOrNull { it.type == TransactionType.INCOME }
            val isTransfer = groupTxs.size == 2 && groupTxs.all { it.installmentNumber == null } &&
                    expenseLeg != null && incomeLeg != null

            if (isTransfer && expenseLeg != null && incomeLeg != null) {
                val fromAccount = accountsList.firstOrNull { it.id == expenseLeg.accountId }
                val toAccount = accountsList.firstOrNull { it.id == incomeLeg.accountId }
                val titlePrefix = "Transferência para ${toAccount?.name ?: ""}"
                val extraDescription = if (toAccount != null && expenseLeg.title.startsWith(titlePrefix)) {
                    expenseLeg.title.removePrefix(titlePrefix).removePrefix(" - ")
                } else {
                    expenseLeg.title.substringAfter(" - ", "")
                }

                editingTransferLegs = expenseLeg to incomeLeg
                formState.value = formState.value.copy(
                    editingTransaction = transaction,
                    isTransfer = true,
                    isRecurring = false,
                    type = TransactionType.EXPENSE,
                    description = extraDescription,
                    amountDigits = Math.round(expenseLeg.amount * 100).toString(),
                    selectedAccount = fromAccount,
                    selectedDestinationAccount = toAccount,
                    totalInstallments = 1,
                    dateMillis = expenseLeg.date,
                    error = null
                )
                return@launch
            }

            val cleanDescription = if (isGroup) {
                transaction.title.replace(Regex("""\s*\(\d+/\d+\)$"""), "")
            } else {
                transaction.title
            }

            val totalGroupAmount = if (groupTxs.isNotEmpty()) {
                groupTxs.sumOf { it.amount }
            } else {
                transaction.amount
            }

            val totalInstallmentsCount = if (groupTxs.isNotEmpty()) {
                groupTxs.maxOfOrNull { it.totalInstallments ?: 1 } ?: (transaction.totalInstallments ?: 1)
            } else {
                transaction.totalInstallments ?: 1
            }

            val baseDate = if (groupTxs.isNotEmpty()) {
                groupTxs.minByOrNull { it.installmentNumber ?: 1 }?.date ?: transaction.date
            } else {
                transaction.date
            }

            // Lançamentos gerados por recorrências antigas ficaram com "Crédito" mesmo
            // saindo de uma conta: corrige para a edição não jogar para o cartão
            // Também troca a forma que não combina com a conta (ex.: Pix na carteira):
            // a conta é mantida, pois é ela que afeta o saldo
            val accountType = accountsList.firstOrNull { it.id == transaction.accountId }?.type
            val hasInvalidPaymentMethod = transaction.creditCardId == null && (
                    transaction.paymentMethod.isCredit() ||
                            !transaction.paymentMethod.isValidFor(transaction.type) ||
                            (accountType != null && !transaction.paymentMethod.isCompatibleWith(accountType))
                    )
            val paymentMethod = if (hasInvalidPaymentMethod) {
                paymentMethodRepository.paymentMethods.first().firstNonCredit(accountType, transaction.type)
                    ?: transaction.paymentMethod
            } else {
                transaction.paymentMethod
            }

            // Regra da recorrência (se ainda ativa): os campos dela aparecem na edição
            val linkedRecurring = transaction.recurringTransactionId
                ?.let { recurringTransactionRepository.getRecurringTransactionById(it) }
                ?.takeIf { it.isActive }
            val hasActiveRecurrence = linkedRecurring != null

            formState.value = formState.value.copy(
                editingTransaction = transaction,
                hasActiveRecurrence = hasActiveRecurrence,
                description = cleanDescription,
                amountDigits = Math.round(totalGroupAmount * 100).toString(),
                type = transaction.type,
                isTransfer = false,
                isRecurring = transaction.recurringTransactionId != null,
                recurringDayOfMonth = linkedRecurring?.dayOfMonth ?: formState.value.recurringDayOfMonth,
                isRecurringInfinite = linkedRecurring?.endDate == null,
                recurringEndDateMillis = linkedRecurring?.endDate,
                // "Sem categoria" (categoria que não existe neste aparelho) não é selecionável:
                // o formulário cai na primeira categoria válida
                selectedCategory = transaction.category.takeIf { it.name != MISSING_CATEGORY_NAME },
                selectedPaymentMethod = paymentMethod,
                selectedAccount = transaction.accountId?.let { id -> accountsList.firstOrNull { it.id == id } },
                selectedCreditCard = transaction.creditCardId?.let { id -> creditCardsList.firstOrNull { it.id == id } },
                totalInstallments = totalInstallmentsCount,
                dateMillis = baseDate,
                error = null
            )
        }
    }

    fun cancelEditing() {
        editingTransferLegs = null
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

    /** Exclui as duas pernas da transferência em edição. */
    fun deleteTransfer(onDone: () -> Unit) {
        val groupId = editingTransferLegs?.first?.installmentGroupId
        viewModelScope.launch {
            if (!groupId.isNullOrEmpty()) {
                transactionRepository.deleteInstallmentGroup(groupId)
            }
            onDone()
        }
    }

    fun deleteInstallmentGroup(groupId: String) {
        viewModelScope.launch {
            transactionRepository.deleteInstallmentGroup(groupId)
        }
    }

    /** Abre a regra de recorrência para edição. */
    fun loadRecurring(id: String, occurrenceDate: Long? = null) {
        viewModelScope.launch {
            val recurring = recurringTransactionRepository.getRecurringTransactionById(id) ?: return@launch
            val categories = categoryRepository.allCategories.first()
            val paymentMethods = paymentMethodRepository.paymentMethods.first()
            val accountsList = accountRepository.allAccounts.first()
            val creditCardsList = creditCardRepository.allCreditCards.first()

            val recurringAccountType = accountsList.firstOrNull { it.id == recurring.accountId }?.type
            val storedPaymentMethod = recurring.resolvePaymentMethod(paymentMethods, recurringAccountType)
            // Forma salva que não combina com a conta: troca a forma e mantém a conta
            val recurringPaymentMethod = if (
                recurring.creditCardId == null && recurringAccountType != null && storedPaymentMethod != null &&
                !storedPaymentMethod.isCompatibleWith(recurringAccountType)
            ) {
                paymentMethods.firstNonCredit(recurringAccountType, recurring.type)
            } else {
                storedPaymentMethod
            }

            formState.value = formState.value.copy(
                editingRecurring = recurring,
                recurringOccurrenceDate = occurrenceDate,
                description = recurring.title,
                amountDigits = Math.round(recurring.amount * 100).toString(),
                type = recurring.type,
                isTransfer = false,
                isRecurring = true,
                recurringDayOfMonth = recurring.dayOfMonth,
                isRecurringInfinite = recurring.endDate == null,
                recurringEndDateMillis = recurring.endDate,
                selectedCategory = categories.firstOrNull { it.id == recurring.categoryId },
                selectedPaymentMethod = recurringPaymentMethod,
                selectedAccount = recurring.accountId?.let { accId -> accountsList.firstOrNull { it.id == accId } },
                selectedCreditCard = recurring.creditCardId?.let { cardId -> creditCardsList.firstOrNull { it.id == cardId } },
                totalInstallments = 1,
                dateMillis = recurring.startDate,
                error = null
            )
        }
    }

    /**
     * "Excluir apenas este lançamento": remove só a ocorrência aberta.
     * Para um agendado (que ainda não existe no banco) grava um lançamento já
     * excluído, que serve de marcador para o mês não ser gerado nem projetado.
     */
    fun deleteOnlyThisOccurrence(onDone: () -> Unit) {
        val state = uiState.value
        val editingTransaction = state.editingTransaction
        val recurring = state.editingRecurring
        val occurrenceDate = state.recurringOccurrenceDate

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (editingTransaction != null) {
                transactionRepository.deleteTransaction(
                    editingTransaction.copy(
                        deletedAt = now,
                        updatedAt = now,
                        syncStatus = SyncStatus.PENDING_DELETE
                    )
                )
            } else if (recurring != null && occurrenceDate != null) {
                val category = categoryRepository.allCategories.first()
                    .let { all -> all.firstOrNull { it.id == recurring.categoryId } ?: all.firstOrNull() }
                val paymentMethod = recurring.resolvePaymentMethod(paymentMethodRepository.allPaymentMethods.first())

                if (category != null && paymentMethod != null) {
                    val occurrenceCal = Calendar.getInstance().apply { timeInMillis = occurrenceDate }
                    transactionRepository.addTransaction(
                        Transaction(
                            // mesmo id que o lançamento teria se fosse gerado
                            id = occurrenceId(
                                recurring.id,
                                occurrenceCal.get(Calendar.YEAR),
                                occurrenceCal.get(Calendar.MONTH)
                            ),
                            userId = authRepository.currentUser?.uid ?: "",
                            title = recurring.title,
                            amount = recurring.amount,
                            type = recurring.type,
                            category = category,
                            paymentMethod = paymentMethod,
                            date = occurrenceDate,
                            updatedAt = now,
                            deletedAt = now,
                            syncStatus = SyncStatus.PENDING_DELETE,
                            accountId = recurring.accountId,
                            creditCardId = recurring.creditCardId,
                            recurringTransactionId = recurring.id
                        )
                    )
                }
            }
            onDone()
        }
    }

    /**
     * "Excluir todos os lançamentos agendados": cancela a recorrência (o histórico
     * já lançado é mantido). Se o usuário abriu um lançamento já existente, ele
     * também é excluído.
     */
    fun deleteAllScheduledOccurrences(onDone: () -> Unit) {
        val state = uiState.value
        val editingTransaction = state.editingTransaction

        viewModelScope.launch {
            val recurring = state.editingRecurring
                ?: editingTransaction?.recurringTransactionId
                    ?.let { recurringTransactionRepository.getRecurringTransactionById(it) }

            if (editingTransaction != null) {
                val now = System.currentTimeMillis()
                transactionRepository.deleteTransaction(
                    editingTransaction.copy(
                        deletedAt = now,
                        updatedAt = now,
                        syncStatus = SyncStatus.PENDING_DELETE
                    )
                )
            }

            if (recurring != null) {
                recurringTransactionRepository.deleteRecurringTransaction(recurring, deletePastHistory = false)
            }
            onDone()
        }
    }

    fun loadTransaction(id: String) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(id)
            transaction?.let { startEditing(it) }
        }
    }

    /**
     * @param applyToRecurrence ao editar um lançamento recorrente, true = "este e os próximos"
     * (atualiza também a regra); false = só o lançamento aberto.
     */
    fun saveTransaction(applyToRecurrence: Boolean = false, onSaved: () -> Unit) {
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

            val editingLegs = editingTransferLegs

            viewModelScope.launch {
                // Ao editar, desconsidera a própria transferência no cálculo do saldo
                val allTransactions = transactionRepository.transactions.first().filter {
                    it.id != editingLegs?.first?.id && it.id != editingLegs?.second?.id
                }
                val calculateBalance = CalculateAccountBalanceUseCase()
                val availableBalance = calculateBalance(fromAccount, allTransactions)

                if (amount > availableBalance) {
                    val formattedBalance = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(availableBalance)
                    formState.value = formState.value.copy(
                        error = "Não é possível transferir por falta de saldo na conta ${fromAccount.name} ($formattedBalance disponível)"
                    )
                    return@launch
                }

                val category = editingLegs?.first?.category
                    ?: state.categories.firstOrNull { it.name.contains("Transferência", ignoreCase = true) }
                    ?: state.categories.firstOrNull()

                val paymentMethod = editingLegs?.first?.paymentMethod ?: state.paymentMethods.firstOrNull { it.name.contains("Transferência", ignoreCase = true) || it.name.contains("Pix", ignoreCase = true) }
                    ?: state.paymentMethods.firstOrNull()

                if (category == null || paymentMethod == null) {
                    formState.value = formState.value.copy(error = "Categoria ou forma de pagamento indisponível")
                    return@launch
                }

                val groupId = editingLegs?.first?.installmentGroupId ?: UUID.randomUUID().toString()
                val extraDesc = if (state.description.isNotBlank()) " - ${state.description}" else ""

                val sourceTx = Transaction(
                    id = editingLegs?.first?.id ?: UUID.randomUUID().toString(),
                    remoteId = editingLegs?.first?.remoteId,
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
                    id = editingLegs?.second?.id ?: UUID.randomUUID().toString(),
                    remoteId = editingLegs?.second?.remoteId,
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

                if (editingLegs != null) {
                    transactionRepository.updateTransaction(sourceTx)
                    transactionRepository.updateTransaction(destTx)
                } else {
                    transactionRepository.addTransactions(listOf(sourceTx, destTx))
                }
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

        // Editando a regra de recorrência: atualiza a regra (vale para os próximos lançamentos)
        val editingRecurring = state.editingRecurring
        if (editingRecurring != null) {
            if (isCreditCardPayment && state.selectedCreditCard == null) {
                formState.value = formState.value.copy(error = "Selecione um cartão de crédito")
                return
            }
            if (!isCreditCardPayment && state.selectedAccount == null) {
                formState.value = formState.value.copy(error = "Selecione uma conta bancária")
                return
            }

            viewModelScope.launch {
                recurringTransactionRepository.updateRecurringTransaction(
                    editingRecurring.copy(
                        title = state.description,
                        amount = amount,
                        type = state.type,
                        categoryId = category.id,
                        paymentMethodId = paymentMethod.id,
                        accountId = if (!isCreditCardPayment) state.selectedAccount?.id else null,
                        creditCardId = if (isCreditCardPayment) state.selectedCreditCard?.id else null,
                        dayOfMonth = state.recurringDayOfMonth,
                        endDate = if (state.isRecurringInfinite) null else state.recurringEndDateMillis
                    )
                )
                onSaved()
            }
            return
        }

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

            // "Este e os próximos": atualiza a regra, então os agendados passam a refletir a mudança
            val linkedRecurringId = editingTransaction?.recurringTransactionId
            val updatesRecurrence = applyToRecurrence && linkedRecurringId != null
            if (updatesRecurrence && linkedRecurringId != null) {
                recurringTransactionRepository.getRecurringTransactionById(linkedRecurringId)?.let { rule ->
                    recurringTransactionRepository.updateRecurringTransaction(
                        rule.copy(
                            title = state.description,
                            amount = amount,
                            type = state.type,
                            categoryId = category.id,
                            paymentMethodId = paymentMethod.id,
                            accountId = if (!isCreditCardPayment) state.selectedAccount?.id else null,
                            creditCardId = if (isCreditCardPayment) state.selectedCreditCard?.id else null,
                            dayOfMonth = state.recurringDayOfMonth,
                            endDate = if (state.isRecurringInfinite) null else state.recurringEndDateMillis
                        )
                    )
                }
            }

            if (installmentsCount > 1 && isCreditCardPayment) {
                val totalCents = Math.round(amount * 100)
                val baseInstallmentCents = totalCents / installmentsCount
                val remainderCents = totalCents % installmentsCount

                // Se estiver editando um parcelamento existente, remove o grupo antigo antes de inserir o novo
                val oldGroupId = editingTransaction?.installmentGroupId
                if (!oldGroupId.isNullOrEmpty()) {
                    transactionRepository.deleteInstallmentGroup(oldGroupId)
                }

                val groupId = oldGroupId ?: UUID.randomUUID().toString()

                val card = state.selectedCreditCard
                val cal = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                val isAfterClosing = editingTransaction == null && card != null && dayOfMonth > card.closingDay

                // Se a fatura já fechou no dia do lançamento, a primeira parcela vai para o mês seguinte
                val firstInstallmentDate = if (isAfterClosing) addMonthsToDate(state.dateMillis, 1) else state.dateMillis

                val installmentsList = (1..installmentsCount).map { i ->
                    val installmentAmount = if (i == 1) {
                        (baseInstallmentCents + remainderCents) / 100.0
                    } else {
                        baseInstallmentCents / 100.0
                    }
                    val installmentDate = addMonthsToDate(firstInstallmentDate, i - 1)
                    Transaction(
                        id = UUID.randomUUID().toString(),
                        userId = currentUserId,
                        title = "${state.description} ($i/$installmentsCount)",
                        amount = installmentAmount,
                        type = state.type,
                        category = category,
                        paymentMethod = paymentMethod,
                        date = installmentDate,
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.PENDING_CREATE,
                        creditCardId = state.selectedCreditCard?.id,
                        installmentNumber = i,
                        totalInstallments = installmentsCount,
                        installmentGroupId = groupId
                    )
                }

                transactionRepository.addTransactions(installmentsList)
            } else {
                // Se era um parcelamento antes e virou 1x (à vista), deleta o grupo antigo
                val oldGroupId = editingTransaction?.installmentGroupId
                if (!oldGroupId.isNullOrEmpty()) {
                    transactionRepository.deleteInstallmentGroup(oldGroupId)
                }

                val card = state.selectedCreditCard
                val cal = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                val isAfterClosing = editingTransaction == null && isCreditCardPayment && card != null && dayOfMonth > card.closingDay
                val transactionDate = when {
                    isAfterClosing -> addMonthsToDate(state.dateMillis, 1)
                    // o lançamento aberto acompanha o novo dia da recorrência
                    updatesRecurrence -> withDayOfMonth(state.dateMillis, state.recurringDayOfMonth)
                    else -> state.dateMillis
                }

                val transaction = Transaction(
                    id = editingTransaction?.id ?: UUID.randomUUID().toString(),
                    userId = currentUserId,
                    title = state.description,
                    amount = amount,
                    type = state.type,
                    category = category,
                    paymentMethod = paymentMethod,
                    date = transactionDate,
                    updatedAt = System.currentTimeMillis(),
                    deletedAt = editingTransaction?.deletedAt,
                    syncStatus = if (editingTransaction == null) SyncStatus.PENDING_CREATE else SyncStatus.PENDING_UPDATE,
                    remoteId = editingTransaction?.remoteId,
                    accountId = if (!isCreditCardPayment) state.selectedAccount?.id else null,
                    creditCardId = if (isCreditCardPayment) state.selectedCreditCard?.id else null,
                    installmentNumber = if (isCreditCardPayment) 1 else null,
                    totalInstallments = if (isCreditCardPayment) 1 else null,
                    // mantém o vínculo com a recorrência, senão o mês seria gerado de novo
                    recurringTransactionId = editingTransaction?.recurringTransactionId
                )

                if (editingTransaction == null || !oldGroupId.isNullOrEmpty()) {
                    transactionRepository.addTransaction(transaction)
                } else {
                    transactionRepository.updateTransaction(transaction)
                }
            }
            onSaved()
        }
    }

    /** Converte a data escolhida no DatePicker (meia-noite UTC) para o mesmo dia no fuso local. */
    private fun pickerDateToLocal(utcMillis: Long, hour: Int, minute: Int): Long {
        val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
        return Calendar.getInstance().apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
        }.timeInMillis
    }

    /** Mesma data, trocando só o dia do mês (limitado ao último dia do mês). */
    private fun withDayOfMonth(dateMillis: Long, day: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.DAY_OF_MONTH, day.coerceIn(1, getActualMaximum(Calendar.DAY_OF_MONTH)))
        }.timeInMillis
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
