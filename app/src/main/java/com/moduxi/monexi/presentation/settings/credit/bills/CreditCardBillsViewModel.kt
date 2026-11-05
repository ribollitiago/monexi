package com.moduxi.monexi.presentation.settings.credit.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.creditcard.CreditCardBill
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.usecase.CalculateCreditCardBillsUseCase
import com.moduxi.monexi.domain.usecase.PayCreditCardBillUseCase
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreditCardBillsViewModel(
    private val creditCardRepository: CreditCardRepository,
    private val accountRepository: AccountRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val creditCardId: String = savedStateHandle.get<String>("cardId") ?: ""
    private val calculateBillsUseCase = CalculateCreditCardBillsUseCase()
    private val payBillUseCase = PayCreditCardBillUseCase(transactionRepository)

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState = combine(
        creditCardRepository.allCreditCards,
        transactionRepository.transactions,
        accountRepository.accounts,
        paymentMethodRepository.allPaymentMethods
    ) { cards, transactions, accounts, paymentMethods ->
        val card = cards.firstOrNull { it.id == creditCardId }
        val bills = if (card != null) calculateBillsUseCase(card, transactions) else emptyList()

        CreditCardBillsUiState(
            creditCard = card,
            bills = bills,
            accounts = accounts,
            paymentMethods = paymentMethods,
            isLoading = _isLoading.value,
            error = _error.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreditCardBillsUiState()
    )

    fun payBill(bill: CreditCardBill, accountId: String, paymentMethod: PaymentMethod, onPaid: () -> Unit) {
        val card = uiState.value.creditCard ?: return
        val currentUserId = authRepository.currentUser?.uid ?: ""

        viewModelScope.launch {
            try {
                payBillUseCase(bill, card, accountId, paymentMethod, currentUserId)
                onPaid()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Erro ao pagar fatura"
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                val savedStateHandle = createSavedStateHandle()
                CreditCardBillsViewModel(
                    creditCardRepository = application.creditCardRepository,
                    accountRepository = application.accountRepository,
                    paymentMethodRepository = application.paymentMethodRepository,
                    transactionRepository = application.transactionRepository,
                    authRepository = application.authRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
