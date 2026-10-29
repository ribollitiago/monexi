package com.moduxi.monexi.presentation.settings.credit.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditCreditCardViewModel(
    private val creditCardRepository: CreditCardRepository,
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditCreditCardUiState())
    val uiState: StateFlow<EditCreditCardUiState> = _uiState.asStateFlow()

    init {
        val creditCardId: String? = savedStateHandle.get<String>("id")
        viewModelScope.launch {
            accountRepository.accounts.collect { accounts ->
                _uiState.value = _uiState.value.copy(
                    accounts = accounts
                )
            }
        }
        if (!creditCardId.isNullOrEmpty()) {
            loadCreditCard(creditCardId)
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            creditCardName = name,
            error = null
        )
    }

    fun onLimitAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(
            limitDigits = value.filter { it.isDigit() }.take(12),
            error = null
        )
    }

    fun onClosingDayChange(dayStr: String) {
        val day = dayStr.filter { it.isDigit() }.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(
            creditCardClosingDay = day.coerceIn(0, 31),
            error = null
        )
    }

    fun onDueDayChange(dayStr: String) {
        val day = dayStr.filter { it.isDigit() }.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(
            creditCardDueDay = day.coerceIn(0, 31),
            error = null
        )
    }

    fun onColorChange(colorHex: String) {
        _uiState.value = _uiState.value.copy(
            selectedColorHex = colorHex,
            error = null
        )
    }

    fun onIconChange(iconName: String) {
        _uiState.value = _uiState.value.copy(
            selectedIconName = iconName,
            error = null
        )
    }

    fun onAccountChange(account: Account?) {
        _uiState.value = _uiState.value.copy(
            selectedAccount = account
        )
    }

    fun loadCreditCard(id: String) {
        viewModelScope.launch {
            val card = creditCardRepository.getCreditCardById(id)
            val accountsList = accountRepository.allAccounts.first()
            card?.let {
                _uiState.value = _uiState.value.copy(
                    editingCreditCard = it,
                    creditCardName = it.name,
                    limitDigits = (it.limitAmount * 100).toLong().toString(),
                    creditCardClosingDay = it.closingDay,
                    creditCardDueDay = it.dueDay,
                    selectedColorHex = it.colorHex,
                    selectedIconName = it.iconName,
                    selectedAccount = it.linkedAccountId?.let { accountId -> accountsList.firstOrNull { acc -> acc.id == accountId } }
                )
            }
        }
    }

    fun saveCreditCard(onSaved: () -> Unit) {
        val state = _uiState.value
        val name = state.creditCardName.trim()
        val limitAmount = (state.limitDigits.toLongOrNull() ?: 0L) / 100.0

        if (name.isBlank()) {
            _uiState.value = state.copy(error = "Informe o nome do cartão de crédito")
            return
        }

        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            val editingCard = state.editingCreditCard

            if (editingCard == null) {
                creditCardRepository.addCreditCard(
                    CreditCard(
                        id = "0",
                        userId = currentUserId,
                        name = name,
                        limitAmount = limitAmount,
                        closingDay = state.creditCardClosingDay,
                        dueDay = state.creditCardDueDay,
                        colorHex = state.selectedColorHex,
                        iconName = state.selectedIconName,
                        linkedAccountId = state.selectedAccount?.id,
                        isArchived = false
                    )
                )
            } else {
                creditCardRepository.updateCreditCard(
                    editingCard.copy(
                        name = name,
                        limitAmount = limitAmount,
                        closingDay = state.creditCardClosingDay,
                        dueDay = state.creditCardDueDay,
                        colorHex = state.selectedColorHex,
                        linkedAccountId = state.selectedAccount?.id,
                        iconName = state.selectedIconName
                    )
                )
            }
            onSaved()
        }
    }

    fun deleteCreditCard(onDeleted: () -> Unit) {
        val card = _uiState.value.editingCreditCard ?: return
        viewModelScope.launch {
            creditCardRepository.deleteCreditCard(card)
            onDeleted()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                val savedStateHandle = createSavedStateHandle()
                EditCreditCardViewModel(
                    creditCardRepository = application.creditCardRepository,
                    accountRepository = application.accountRepository,
                    authRepository = application.authRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
