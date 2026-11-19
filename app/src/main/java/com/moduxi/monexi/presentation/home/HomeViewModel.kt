package com.moduxi.monexi.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.data.repository.local.PrivacyManager
import com.moduxi.monexi.data.sync.SyncManager
import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.RecurringTransactionRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import com.moduxi.monexi.domain.usecase.CalculateAccountBalanceUseCase
import com.moduxi.monexi.domain.usecase.CalculateExpenseUseCase
import com.moduxi.monexi.domain.usecase.CalculateIncomeUseCase
import com.moduxi.monexi.domain.usecase.ProjectRecurringTransactionsUseCase
import com.moduxi.monexi.presentation.settings.accounts.AccountWithBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val creditCardRepository: CreditCardRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val syncManager: SyncManager,
    private val privacyManager: PrivacyManager
) : ViewModel() {

    private val calculateIncomeUseCase = CalculateIncomeUseCase()
    private val calculateExpenseUseCase = CalculateExpenseUseCase()
    private val calculateAccountBalanceUseCase = CalculateAccountBalanceUseCase()
    private val projectRecurringTransactionsUseCase = ProjectRecurringTransactionsUseCase()

    private data class RecurringSources(
        val recurring: List<RecurringTransaction>,
        val categories: List<Category>,
        val paymentMethods: List<PaymentMethod>,
        val deletedOccurrences: List<Transaction>
    )

    private val recurringSources = combine(
        recurringTransactionRepository.activeRecurringTransactions,
        categoryRepository.categories,
        paymentMethodRepository.paymentMethods,
        transactionRepository.deletedRecurringTransactions
    ) { recurring, categories, paymentMethods, deleted ->
        RecurringSources(recurring, categories, paymentMethods, deleted)
    }

    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())

    init {
        syncTransactions()
    }

    val uiState = combine(
        transactionRepository.transactions,
        accountRepository.accounts,
        creditCardRepository.creditCards,
        _selectedCalendar,
        recurringSources,
        privacyManager.isBalanceVisible
    ) { flows: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val allTransactions = flows[0] as List<Transaction>
        @Suppress("UNCHECKED_CAST")
        val allAccounts = flows[1] as List<Account>
        @Suppress("UNCHECKED_CAST")
        val allCreditCards = flows[2] as List<CreditCard>
        val calendar = flows[3] as Calendar
        @Suppress("UNCHECKED_CAST")
        val sources = flows[4] as RecurringSources
        val isBalanceVisible = flows[5] as Boolean

        val selectedYear = calendar.get(Calendar.YEAR)
        val selectedMonth = calendar.get(Calendar.MONTH)

        val monthTransactions = allTransactions.filter { transaction ->
            val transCal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            transCal.get(Calendar.YEAR) == selectedYear && transCal.get(Calendar.MONTH) == selectedMonth
        }

        val now = System.currentTimeMillis()
        val endOfToday = CalculateAccountBalanceUseCase.endOfToday(now)
        val accountsWithBalance = allAccounts.map { account ->
            AccountWithBalance(
                account = account,
                currentBalance = calculateAccountBalanceUseCase(account, allTransactions, endOfToday)
            )
        }

        val endOfMonth = CalculateAccountBalanceUseCase.endOfMonth(now)
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }
        val currentMonthTransactions = allTransactions.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.date }
            c.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) && c.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
        }
        val projections = projectRecurringTransactionsUseCase(
            recurringTransactions = sources.recurring,
            existingTransactions = currentMonthTransactions + sources.deletedOccurrences,
            categories = sources.categories,
            paymentMethods = sources.paymentMethods,
            year = nowCal.get(Calendar.YEAR),
            month = nowCal.get(Calendar.MONTH),
            nowMillis = now
        )
        val forecastTransactions = allTransactions + projections
        val projectedNetWorth = allAccounts
            .filter { !it.excludeFromTotal }
            .sumOf { calculateAccountBalanceUseCase(it, forecastTransactions, endOfMonth) }

        val netWorth = accountsWithBalance
            .filter { !it.account.excludeFromTotal }
            .sumOf { it.currentBalance }

        val benefitsTotal = accountsWithBalance
            .filter { it.account.excludeFromTotal }
            .sumOf { it.currentBalance}

        HomeUiState(
            netWorth = netWorth,
            projectedNetWorth = projectedNetWorth,
            benefitsTotal = benefitsTotal,
            accounts = accountsWithBalance,
            creditCards = allCreditCards.filter { !it.isArchived },
            totalIncome = calculateIncomeUseCase(monthTransactions),
            totalExpense = calculateExpenseUseCase(monthTransactions),
            selectedMonthName = formatMonthYear(calendar.time),
            transactions = monthTransactions,
            isBalanceVisible = isBalanceVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun previousMonth() {
        val newCal = (_selectedCalendar.value.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        _selectedCalendar.value = newCal
    }

    fun nextMonth() {
        val newCal = (_selectedCalendar.value.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        _selectedCalendar.value = newCal
    }

    fun toggleBalanceVisibility() {
        viewModelScope.launch {
            privacyManager.setBalanceVisible(!uiState.value.isBalanceVisible)
        }
    }

    private fun formatMonthYear(date: Date): String {
        val formatter = SimpleDateFormat("MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
        val formatted = formatter.format(date)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("pt-BR")) else it.toString() }
    }

    fun syncTransactions() {
        viewModelScope.launch {
            syncManager.syncAll()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication
                HomeViewModel(
                    application.transactionRepository,
                    application.accountRepository,
                    application.creditCardRepository,
                    application.recurringTransactionRepository,
                    application.categoryRepository,
                    application.paymentMethodRepository,
                    application.syncManager,
                    application.privacyManager
                )
            }
        }
    }
}
