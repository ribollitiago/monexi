package com.moduxi.monexi.domain.model.recurring

import com.moduxi.monexi.domain.model.transaction.TransactionType

data class RecurringTransaction(
    val id: String,
    val userId: String,
    val title: String,               // "Salário", "Netflix", "VR Alelo"
    val amount: Double,
    val type: TransactionType,       // INCOME ou EXPENSE
    val categoryId: String,
    val accountId: String?,          // Conta destino (ex: Nubank para salário)
    val creditCardId: String?,       // Ou cartão (ex: Netflix no cartão)
    val dayOfMonth: Int,             // Dia que acontece (5, 10, 28...)
    val frequency: Frequency,
    val startDate: Long,             // Quando começou
    val endDate: Long?,              // null = sem data de fim
    val isActive: Boolean,
)
