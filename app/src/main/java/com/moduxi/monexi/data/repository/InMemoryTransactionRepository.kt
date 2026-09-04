package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object InMemoryTransactionRepository : TransactionRepository {

    private val initialTransactions = listOf(
        Transaction(
            id = 1,
            title = "Salario",
            amount = 3200.0,
            type = TransactionType.INCOME,
            category = Category(id = 1, name = "Trabalho", isDefault = true),
            paymentMethod = PaymentMethod(id = 2, name = "Pix", isDefault = true),
            date = System.currentTimeMillis()
        ),
        Transaction(
            id = 2,
            title = "Mercado",
            amount = 280.0,
            type = TransactionType.EXPENSE,
            category = Category(id = 2, name = "Alimentacao", isDefault = true),
            paymentMethod = PaymentMethod(id = 3, name = "Cartao de debito", isDefault = true),
            date = System.currentTimeMillis()
        ),
        Transaction(
            id = 3,
            title = "Internet",
            amount = 120.0,
            type = TransactionType.EXPENSE,
            category = Category(id = 3, name = "Casa", isDefault = true),
            paymentMethod = PaymentMethod(id = 2, name = "Pix", isDefault = true),
            date = System.currentTimeMillis()
        )
    )

    private val _transactions = MutableStateFlow(initialTransactions)
    override val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    override fun addTransaction(transaction: Transaction) {
        _transactions.value += transaction
    }
}