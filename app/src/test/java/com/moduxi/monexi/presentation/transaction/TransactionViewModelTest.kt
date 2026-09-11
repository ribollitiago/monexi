package com.moduxi.monexi.presentation.transaction

import com.moduxi.monexi.MainDispatcherRule
import com.moduxi.monexi.data.repository.FakeCategoryRepository
import com.moduxi.monexi.data.repository.FakePaymentMethodRepository
import com.moduxi.monexi.data.repository.FakeTransactionRepository
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `should save transaction`() = runTest {
        val transactionRepository = FakeTransactionRepository()

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Alimentação",
                    type = TransactionType.EXPENSE
                )
            )
        )
        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                PaymentMethod(
                    id = 1,
                    name = "Pix"
                )
            )
        )
        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        var wasSaved = false

        viewModel.onDescriptionChange("Mercado")
        viewModel.onAmountChange("2500")
        viewModel.saveTransaction(
            onSaved = {
                wasSaved = true
            }
        )

        advanceUntilIdle()

        val transactions = transactionRepository.currentTransactions

        assertEquals(1, transactions.size)
        assertEquals("Mercado", transactions.first().title)
        assertEquals(25.0, transactions.first().amount, 0.0)
        assertEquals(TransactionType.EXPENSE, transactions.first().type)
        assertEquals("Alimentação", transactions.first().category.name)
        assertEquals("Pix", transactions.first().paymentMethod.name)
        assertEquals(true, wasSaved)
    }

    @Test
    fun `should not save transaction without description`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Alimentação",
                    type = TransactionType.EXPENSE
                )
            )
        )
        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                PaymentMethod(
                    id = 1,
                    name = "Pix"
                )
            )
        )
        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        var wasSaved = false

        viewModel.onAmountChange("2500")
        viewModel.saveTransaction(
            onSaved = {
                wasSaved = true
            }
        )

        advanceUntilIdle()

        assertEquals(0, transactionRepository.currentTransactions.size)
        assertEquals("Informe a descrição", viewModel.uiState.value.error)
        assertFalse(wasSaved)
    }

    @Test
    fun `should not save transaction without amount`() = runTest {
        val transactionRepository = FakeTransactionRepository()

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Alimentação",
                    type = TransactionType.EXPENSE
                )
            )
        )
        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                PaymentMethod(
                    id = 1,
                    name = "Pix"
                )
            )
        )
        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        var wasSave = false

        viewModel.onDescriptionChange("Conta de Água")
        viewModel.saveTransaction(
            onSaved = {
                wasSave = true
            }
        )

        advanceUntilIdle()

        assertEquals(0, transactionRepository.currentTransactions.size)
        assertEquals("Informe um valor válido", viewModel.uiState.value.error)
        assertFalse(wasSave)
    }

    @Test
    fun `should not save transaction without category`() = runTest {
        val transactionRepository = FakeTransactionRepository()

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf()
        )
        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                PaymentMethod(
                    id = 1,
                    name = "Pix"
                )
            )
        )
        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        var wasSave = false

        viewModel.onDescriptionChange("Conta de Água")
        viewModel.onAmountChange("2500.00")
        viewModel.saveTransaction(
            onSaved = {
                wasSave = true
            }
        )

        advanceUntilIdle()

        assertEquals(0, transactionRepository.currentTransactions.size)
        assertEquals("Selecione uma categoria", viewModel.uiState.value.error)
        assertFalse(wasSave)
    }

    @Test
    fun `should not save transaction without payment method`() = runTest {
        val transactionRepository = FakeTransactionRepository()

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Alimentação",
                    type = TransactionType.EXPENSE
                )
            )
        )
        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf()
        )
        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        var wasSave = false

        viewModel.onDescriptionChange("Conta de Água")
        viewModel.onAmountChange("2500.00")
        viewModel.saveTransaction(
            onSaved = {
                wasSave = true
            }
        )

        advanceUntilIdle()

        assertEquals(0, transactionRepository.currentTransactions.size)
        assertEquals("Selecione uma forma de pagamento", viewModel.uiState.value.error)
        assertFalse(wasSave)
    }

    @Test
    fun `should filter categories by transaction type`() = runTest {
        val transactionRepository = FakeTransactionRepository()

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Salário",
                    type = TransactionType.INCOME
                ),
                Category(
                    id = 2,
                    name = "Alimentação",
                    type = TransactionType.EXPENSE
                )

            )
        )
        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                PaymentMethod(
                    id = 1,
                    name = "Pix"
                )
            )
        )
        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.categories.size)
        assertEquals("Alimentação", viewModel.uiState.value.categories.first().name)
        assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.categories.first().type)

        viewModel.onTypeChange(TransactionType.INCOME)

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.categories.size)
        assertEquals("Salário", viewModel.uiState.value.categories.first().name)
        assertEquals(TransactionType.INCOME, viewModel.uiState.value.categories.first().type)
    }

    @Test
    fun `should start editing transaction`() = runTest {
        val transactionRepository = FakeTransactionRepository()

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Salário",
                    type = TransactionType.INCOME
                ),
                Category(
                    id = 2,
                    name = "Alimentação",
                    type = TransactionType.EXPENSE
                )

            )
        )
        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                PaymentMethod(
                    id = 1,
                    name = "Pix"
                )
            )
        )
        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
    }
}
