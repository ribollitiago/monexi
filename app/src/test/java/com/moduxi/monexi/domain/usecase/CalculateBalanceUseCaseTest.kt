package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateBalanceUseCaseTest {

    private val useCase = CalculateBalanceUseCase()
    private val category = Category(
        id = 1,
        name = "Teste"
    )

    private val paymentMethod = PaymentMethod(
        id = 1,
        name = "Pix"
    )

    @Test
    fun `should calculate balance`() {
        val transactions = listOf(
            Transaction(
                id = 1,
                title = "Salario",
                amount = 3000.0,
                type = TransactionType.INCOME,
                category = category,
                paymentMethod = paymentMethod,
                date = 0L
            ),
            Transaction(
                id = 2,
                title = "Mercado",
                amount = 250.0,
                type = TransactionType.EXPENSE,
                category = category,
                paymentMethod = paymentMethod,
                date = 0L
            ),
            Transaction(
                id = 3,
                title = "Internet",
                amount = 120.0,
                type = TransactionType.EXPENSE,
                category = category,
                paymentMethod = paymentMethod,
                date = 0L
            )
        )

        val result = useCase(transactions)

        assertEquals(2630.0, result, 0.0)
    }
}