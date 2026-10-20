package com.moduxi.monexi.presentation.extract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExtractViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())

    val uiState = combine(
        transactionRepository.transactions,
        _searchQuery,
        _selectedCalendar
    ) { transactions, query, calendar ->
        val selectedYear = calendar.get(Calendar.YEAR)
        val selectedMonth = calendar.get(Calendar.MONTH)

        val monthFilteredTransactions = transactions.filter { transaction ->
            val transCal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            transCal.get(Calendar.YEAR) == selectedYear && transCal.get(Calendar.MONTH) == selectedMonth
        }

        ExtractUiState(
            search = query,
            selectedMonthName = formatMonthYear(calendar.time),
            transactions = monthFilteredTransactions
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
                ExtractViewModel(application.transactionRepository)
            }
        }
    }
}
