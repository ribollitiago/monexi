package com.moduxi.monexi.presentation.home

import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.presentation.settings.accounts.AccountWithBalance

data class HomeUiState(
    val netWorth: Double = 0.0,
    val benefitsTotal: Double = 0.0,
    /** Patrimônio previsto no fim do mês (saldo de hoje + agendados do mês). */
    val projectedNetWorth: Double = 0.0,
    val accounts: List<AccountWithBalance> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val selectedMonthName: String = "",
    val transactions: List<Transaction> = emptyList(),
    val isBalanceVisible: Boolean = true
)
