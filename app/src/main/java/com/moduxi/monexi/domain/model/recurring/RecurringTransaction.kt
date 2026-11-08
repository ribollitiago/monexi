package com.moduxi.monexi.domain.model.recurring

import com.moduxi.monexi.domain.model.transaction.TransactionType

data class RecurringTransaction(
    val id: String,
    val userId: String,
    val title: String,               // "Salário", "Netflix", "VR Alelo"
    val amount: Double,
    val type: TransactionType,       // INCOME ou EXPENSE
    val categoryId: String,
    val paymentMethodId: String? = null,
    val accountId: String? = null,    // Conta destino (ex: Nubank para salário)
    val creditCardId: String? = null, // Ou cartão (ex: Netflix no cartão)
    val dayOfMonth: Int,             // Dia que acontece (5, 10, 28...)
    val frequency: Frequency = Frequency.MONTHLY,
    val startDate: Long = System.currentTimeMillis(), // Quando começou
    val endDate: Long? = null,       // null = sem data de fim
    val isActive: Boolean = true
)
