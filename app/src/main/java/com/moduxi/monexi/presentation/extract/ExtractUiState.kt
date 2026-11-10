package com.moduxi.monexi.presentation.extract

import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.transaction.Transaction

data class ExtractUiState(
    val search: String = "",
    val selectedMonthName: String = "",
    val transactions: List<Transaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val creditCards: List<CreditCard> = emptyList()
)
