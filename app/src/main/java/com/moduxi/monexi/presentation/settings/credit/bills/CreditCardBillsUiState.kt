package com.moduxi.monexi.presentation.settings.credit.bills

import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.creditcard.CreditCardBill
import com.moduxi.monexi.domain.model.payment.PaymentMethod

data class CreditCardBillsUiState(
    val creditCard: CreditCard? = null,
    val bills: List<CreditCardBill> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
}