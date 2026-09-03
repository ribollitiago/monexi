package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateIncomeUseCaseTest {
    private val useCase = CalculateIncomeUseCase()

    private val category = Category(
        id = 1,
        name = "Teste"
    )

    private val paymentMethod = PaymentMethod(
        id = 1,
        name = "Pix"
    )

    @Test
    fun `should calculate total income`() {
        val transactions = listOf(
            Transaction(
                id = 1,
                title = "salario",
                amount = 3000.0,
                type = TransactionType.INCOME,
                category = category,
                paymentMethod = paymentMethod,
                date = 0L
            ),
            Transaction(
                id = 2,
                title = "Freelance",
                amount = 800.0,
                type = TransactionType.INCOME,
                category = category,
                paymentMethod = paymentMethod,
                date = 0L
            ),
            Transaction(
                id = 3,
                title = "Mercado",
                amount = 250.0,
                type = TransactionType.EXPENSE,
                category = category,
                paymentMethod = paymentMethod,
                date = 0L
            )
        )

        val result = useCase(transactions)

        assertEquals(3800.0, result, 0.0)
    }
}