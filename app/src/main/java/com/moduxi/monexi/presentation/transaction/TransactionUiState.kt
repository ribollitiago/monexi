package com.moduxi.monexi.presentation.transaction

import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

data class TransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val description: String = "",
    val amountDigits: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val selectedCategory: Category? = null,
    val selectedPaymentMethod: PaymentMethod? = null,
    val selectedAccount: Account? = null,
    val selectedCreditCard: CreditCard? = null,
    val editingTransaction: Transaction? = null,
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
