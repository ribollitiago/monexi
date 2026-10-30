package com.moduxi.monexi.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import com.moduxi.monexi.domain.usecase.CalculateAccountBalanceUseCase
import com.moduxi.monexi.domain.usecase.CalculateBalanceUseCase
import com.moduxi.monexi.domain.usecase.CalculateExpenseUseCase
import com.moduxi.monexi.domain.usecase.CalculateIncomeUseCase
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
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val calculateIncomeUseCase = CalculateIncomeUseCase()
    private val calculateExpenseUseCase = CalculateExpenseUseCase()
    private val calculateAccountBalanceUseCase = CalculateAccountBalanceUseCase()

    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())

    init {
        syncTransactions()
    }

    val uiState = combine(
        transactionRepository.transactions,
        accountRepository.accounts,
        _selectedCalendar
    ) { allTransactions, allAccounts, calendar ->
        val selectedYear = calendar.get(Calendar.YEAR)
        val selectedMonth = calendar.get(Calendar.MONTH)

        val monthTransactions = allTransactions.filter { transaction ->
            val transCal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            transCal.get(Calendar.YEAR) == selectedYear && transCal.get(Calendar.MONTH) == selectedMonth
        }

        val accountsWithBalance = allAccounts.map { account ->
            AccountWithBalance(
                account = account,
                currentBalance = calculateAccountBalanceUseCase(account, allTransactions)
            )
        }

        val netWorth = accountsWithBalance
            .filter { !it.account.excludeFromTotal }
            .sumOf { it.currentBalance }

        val benefitsTotal = accountsWithBalance
            .filter { it.account.excludeFromTotal }
            .sumOf { it.currentBalance}

        HomeUiState(
            netWorth = netWorth,
            benefitsTotal = benefitsTotal,
            accounts = accountsWithBalance,
            totalIncome = calculateIncomeUseCase(monthTransactions),
            totalExpense = calculateExpenseUseCase(monthTransactions),
            selectedMonthName = formatMonthYear(calendar.time),
            transactions = monthTransactions
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

    private fun formatMonthYear(date: Date): String {
        val formatter = SimpleDateFormat("MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
        val formatted = formatter.format(date)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("pt-BR")) else it.toString() }
    }

    fun syncTransactions() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            transactionRepository.syncFromRemote()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication
                HomeViewModel(
                    application.transactionRepository,
                    application.accountRepository
                    )
            }
        }
    }
}
