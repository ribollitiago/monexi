package com.moduxi.monexi.presentation.settings.accounts.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.Account
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditBankAccountViewModel(
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditBankAccountUiState())
    val uiState: StateFlow<EditBankAccountUiState> = _uiState.asStateFlow()

    init {
        val accountId: String? = savedStateHandle.get<String>("id")
        if (!accountId.isNullOrEmpty()) {
            loadAccount(accountId)
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            accountName = name,
            error = null
        )
    }

    fun onInitialBalanceChange(value: String) {
        _uiState.value = _uiState.value.copy(
            balanceDigits = value.filter { it.isDigit() }.take(12),
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

    fun loadAccount(id: String) {
        viewModelScope.launch {
            val account = accountRepository.getAccountById(id)
            account?.let {
                _uiState.value = _uiState.value.copy(
                    editingBankAccount = it,
                    accountName = it.name,
                    balanceDigits = (it.initialBalance * 100).toLong().toString(),
                    selectedColorHex = it.colorHex,
                    selectedIconName = it.iconName
                )
            }
        }
    }

    fun saveAccount(onSaved: () -> Unit) {
        val state = _uiState.value
        val name = state.accountName.trim()
        val initialBalance = (state.balanceDigits.toLongOrNull() ?: 0L) / 100.0

        if (name.isBlank()) {
            _uiState.value = state.copy(error = "Informe o nome da conta")
            return
        }

        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            val editingAccount = state.editingBankAccount

            if (editingAccount == null) {
                accountRepository.addAccount(
                    Account(
                        id = "0",
                        userId = currentUserId,
                        name = name,
                        initialBalance = initialBalance,
                        colorHex = state.selectedColorHex,
                        iconName = state.selectedIconName,
                        isArchived = false
                    )
                )
            } else {
                accountRepository.updateAccount(
                    editingAccount.copy(
                        name = name,
                        initialBalance = initialBalance,
                        colorHex = state.selectedColorHex,
                        iconName = state.selectedIconName
                    )
                )
            }
            onSaved()
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        val account = _uiState.value.editingBankAccount ?: return
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
            onDeleted()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                val savedStateHandle = createSavedStateHandle()
                EditBankAccountViewModel(
                    accountRepository = application.accountRepository,
                    authRepository = application.authRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
