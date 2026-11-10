package com.moduxi.monexi.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.data.repository.local.OnboardingPrefs
import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val accountRepository: AccountRepository,
    private val creditCardRepository: CreditCardRepository,
    private val categoryRepository: CategoryRepository,
    private val onboardingPrefs: OnboardingPrefs,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateBank(bank: BankDraft) {
        _uiState.update { it.copy(bank = bank, error = null) }
    }

    fun updateCard(card: CardDraft) {
        _uiState.update { it.copy(card = card, error = null) }
    }

    fun onNewCategoryNameChange(name: String) {
        _uiState.update { it.copy(newCategoryName = name, error = null) }
    }

    fun onNewCategoryColorChange(colorHex: String) {
        _uiState.update { it.copy(newCategoryColor = colorHex, error = null) }
    }

    fun addCategory() {
        val name = _uiState.value.newCategoryName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Informe o nome da categoria") }
            return
        }
        val newCat = CategoryDraft(
            name = name,
            colorHex = _uiState.value.newCategoryColor,
            iconName = "Category"
        )
        _uiState.update {
            it.copy(
                categories = it.categories + newCat,
                newCategoryName = "",
                error = null
            )
        }
    }

    fun removeCategory(index: Int) {
        _uiState.update {
            it.copy(categories = it.categories.filterIndexed { idx, _ -> idx != index })
        }
    }

    fun back() {
        _uiState.update { if (it.step > 0) it.copy(step = it.step - 1, error = null) else it }
    }

    fun next(onDone: () -> Unit) {
        val s = _uiState.value
        val err = when (s.step) {
            0 -> validateBank(s.bank)
            1 -> if (s.card.isEmpty) null else validateCard(s.card)
            else -> null
        }
        if (err != null) {
            _uiState.update { it.copy(error = err) }
            return
        }
        if (s.step < 2) {
            _uiState.update { it.copy(step = s.step + 1, error = null) }
        } else {
            finish(onDone)
        }
    }

    private fun validateBank(b: BankDraft): String? {
        return when {
            b.name.isBlank() -> "Informe o nome da conta ou banco"
            else -> null
        }
    }

    private fun validateCard(c: CardDraft): String? {
        val limit = (c.limitDigits.toLongOrNull() ?: 0L) / 100.0
        return when {
            c.name.isBlank() -> "Informe o nome do cartão"
            limit <= 0.0 -> "Limite inválido"
            c.closingDay.toIntOrNull() !in 1..31 -> "Dia de fechamento inválido (1–31)"
            c.dueDay.toIntOrNull() !in 1..31 -> "Dia de vencimento inválido (1–31)"
            else -> null
        }
    }

    private fun finish(onDone: () -> Unit) = viewModelScope.launch {
        val s = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        val currentUserId = authRepository.currentUser?.uid ?: ""
        val initialBalance = (s.bank.balanceDigits.toLongOrNull() ?: 0L) / 100.0

        // 1. Criar Conta Bancária
        val account = Account(
            userId = currentUserId,
            name = s.bank.name.trim(),
            initialBalance = initialBalance,
            colorHex = s.bank.colorHex,
            iconName = s.bank.iconName,
            type = s.bank.type
        )
        accountRepository.addAccount(account)

        // 2. Criar Cartão de Crédito (se preenchido)
        if (!s.card.isEmpty) {
            val limit = (s.card.limitDigits.toLongOrNull() ?: 0L) / 100.0
            val card = CreditCard(
                userId = currentUserId,
                name = s.card.name.trim(),
                limitAmount = limit,
                closingDay = s.card.closingDay.toIntOrNull() ?: 5,
                dueDay = s.card.dueDay.toIntOrNull() ?: 12,
                colorHex = s.card.colorHex,
                iconName = s.card.iconName,
                linkedAccountId = account.id
            )
            creditCardRepository.addCreditCard(card)
        }

        // 3. Criar Categorias Personalizadas
        s.categories.forEach { catDraft ->
            val category = Category(
                userId = currentUserId,
                name = catDraft.name,
                type = TransactionType.EXPENSE,
                colorHex = catDraft.colorHex,
                iconName = catDraft.iconName
            )
            categoryRepository.addCategory(category)
        }

        // 4. Marcar onboarding como concluído para este usuário
        if (currentUserId.isNotEmpty()) {
            onboardingPrefs.setPending(currentUserId, false)
        }

        _uiState.update { it.copy(isSaving = false) }
        onDone()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                OnboardingViewModel(
                    accountRepository = application.accountRepository,
                    creditCardRepository = application.creditCardRepository,
                    categoryRepository = application.categoryRepository,
                    onboardingPrefs = application.onboardingPrefs,
                    authRepository = application.authRepository
                )
            }
        }
    }
}
