package com.moduxi.monexi.presentation.extract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.RecurringTransactionRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import com.moduxi.monexi.domain.usecase.ProjectRecurringTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExtractViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val creditCardRepository: CreditCardRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val projectRecurringTransactionsUseCase = ProjectRecurringTransactionsUseCase()

    private data class RecurringSources(
        val recurring: List<RecurringTransaction>,
        val categories: List<Category>,
        val paymentMethods: List<PaymentMethod>,
        val deletedOccurrences: List<Transaction>
    )

    private data class Filters(val query: String, val calendar: Calendar)

    private val recurringSources = combine(
        recurringTransactionRepository.activeRecurringTransactions,
        categoryRepository.categories,
        paymentMethodRepository.paymentMethods,
        transactionRepository.deletedRecurringTransactions
    ) { recurring, categories, paymentMethods, deletedOccurrences ->
        RecurringSources(recurring, categories, paymentMethods, deletedOccurrences)
    }

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())

    val uiState = combine(
        transactionRepository.transactions,
        accountRepository.allAccounts,
        creditCardRepository.allCreditCards,
        recurringSources,
        combine(_searchQuery, _selectedCalendar) { query, calendar -> Filters(query, calendar) }
    ) { transactions, accounts, creditCards, sources, filters ->
        val query = filters.query
        val calendar = filters.calendar
        val selectedYear = calendar.get(Calendar.YEAR)
        val selectedMonth = calendar.get(Calendar.MONTH)

        val monthFilteredTransactions = transactions.filter { transaction ->
            val transCal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            transCal.get(Calendar.YEAR) == selectedYear && transCal.get(Calendar.MONTH) == selectedMonth
        }

        // Recorrentes ainda não lançadas neste mês entram como "Agendado"
        val scheduledTransactions = projectRecurringTransactionsUseCase(
            recurringTransactions = sources.recurring,
            // ocorrências excluídas pelo usuário também contam, para não reaparecerem
            existingTransactions = monthFilteredTransactions + sources.deletedOccurrences,
            categories = sources.categories,
            paymentMethods = sources.paymentMethods,
            year = selectedYear,
            month = selectedMonth
        )

        ExtractUiState(
            search = query,
            selectedMonthName = formatMonthYear(calendar.time),
            transactions = (monthFilteredTransactions + scheduledTransactions).sortedByDescending { it.date },
            accounts = accounts,
            creditCards = creditCards
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExtractUiState()
    )

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

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

    private fun formatMonthYear(date: Date): String {
        val formatter = SimpleDateFormat("MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
        val formatted = formatter.format(date)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("pt-BR")) else it.toString() }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication
                ExtractViewModel(
                    transactionRepository = application.transactionRepository,
                    accountRepository = application.accountRepository,
                    creditCardRepository = application.creditCardRepository,
                    recurringTransactionRepository = application.recurringTransactionRepository,
                    categoryRepository = application.categoryRepository,
                    paymentMethodRepository = application.paymentMethodRepository
                )
            }
        }
    }
}
