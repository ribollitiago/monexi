package com.moduxi.monexi.presentation.transaction

import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

data class TransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val isTransfer: Boolean = false,
    val isRecurring: Boolean = false,
    val recurringDayOfMonth: Int = 5,
    val isRecurringInfinite: Boolean = true,
    val recurringEndDateMillis: Long? = null,
    val description: String = "",
    val amountDigits: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val totalInstallments: Int = 1,
    val selectedCategory: Category? = null,
    val selectedPaymentMethod: PaymentMethod? = null,
    val selectedAccount: Account? = null,
    val selectedDestinationAccount: Account? = null,
    val sourceAccountBalance: Double? = null,
    val destinationAccountBalance: Double? = null,
    val selectedCreditCard: CreditCard? = null,
    val editingTransaction: Transaction? = null,
    // Edição da regra de recorrência (aberta pela tela de Recorrentes ou por um agendado do extrato)
    val editingRecurring: RecurringTransaction? = null,
    // Data da ocorrência agendada que o usuário abriu no extrato (null se veio das configurações)
    val recurringOccurrenceDate: Long? = null,
    // true quando o lançamento em edição pertence a uma recorrência ainda ativa
    val hasActiveRecurrence: Boolean = false,
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val accounts: List<Account> = emptyList(),
    // Contas compatíveis com a forma de pagamento selecionada (ex.: Dinheiro -> só carteira)
    val availableAccounts: List<Account> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
