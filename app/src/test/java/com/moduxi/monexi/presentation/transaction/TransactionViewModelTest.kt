package com.moduxi.monexi.presentation.transaction

import androidx.lifecycle.SavedStateHandle
import com.moduxi.monexi.MainDispatcherRule
import com.moduxi.monexi.data.repository.FakeCategoryRepository
import com.moduxi.monexi.data.repository.FakePaymentMethodRepository
import com.moduxi.monexi.data.repository.FakeTransactionRepository
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
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
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
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
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        var wasSaved = false

        viewModel.onDescriptionChange("Conta de Água")
        viewModel.saveTransaction(
            onSaved = {
                wasSaved = true
            }
        )

        advanceUntilIdle()

        assertEquals(0, transactionRepository.currentTransactions.size)
        assertEquals("Informe um valor válido", viewModel.uiState.value.error)
        assertFalse(wasSaved)
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
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        var wasSaved = false

        viewModel.onDescriptionChange("Conta de Água")
        viewModel.onAmountChange("2500.00")
        viewModel.saveTransaction(
            onSaved = {
                wasSaved = true
            }
        )

        advanceUntilIdle()

        assertEquals(0, transactionRepository.currentTransactions.size)
        assertEquals("Selecione uma categoria", viewModel.uiState.value.error)
        assertFalse(wasSaved)
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
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        var wasSaved = false

        viewModel.onDescriptionChange("Conta de Água")
        viewModel.onAmountChange("2500.00")
        viewModel.saveTransaction(
            onSaved = {
                wasSaved = true
            }
        )

        advanceUntilIdle()

        assertEquals(0, transactionRepository.currentTransactions.size)
        assertEquals("Selecione uma forma de pagamento", viewModel.uiState.value.error)
        assertFalse(wasSaved)
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
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
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
        val category = Category(
            id = 1,
            name = "Alimentação",
            type = TransactionType.EXPENSE
        )

        val payment = PaymentMethod(
            id = 1,
            name = "Pix"
        )

        val transaction = Transaction(
            id =  1,
            title = "Mercado",
            amount = 25.0,
            type = TransactionType.EXPENSE,
            category = category,
            paymentMethod = payment,
            date = 123456789L
        )

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(category)
        )

        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(payment)
        )

        val transactionRepository = FakeTransactionRepository(
            initialTransactions = listOf(transaction)
        )

        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        viewModel.startEditing(transaction)

        advanceUntilIdle()

        assertEquals(transaction, viewModel.uiState.value.editingTransaction)
        assertEquals("Mercado", viewModel.uiState.value.description)
        assertEquals("2500", viewModel.uiState.value.amountDigits)
        assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.type)
        assertEquals(category, viewModel.uiState.value.selectedCategory)
        assertEquals(payment, viewModel.uiState.value.selectedPaymentMethod)
        assertEquals(123456789L, viewModel.uiState.value.dateMillis)
    }

    @Test
    fun `should delete transaction`() = runTest {
        val category = Category(
            id = 1,
            name = "Alimentação",
            type = TransactionType.EXPENSE
        )

        val payment = PaymentMethod(
            id = 1,
            name = "Pix"
        )

        val transaction = Transaction(
            id =  1,
            title = "Mercado",
            amount = 25.0,
            type = TransactionType.EXPENSE,
            category = category,
            paymentMethod = payment,
            date = 123456789L
        )

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(category)
        )

        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(payment)
        )

        val transactionRepository = FakeTransactionRepository(
            initialTransactions = listOf(transaction)
        )

        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        assertEquals(1, transactionRepository.currentTransactions.size)

        viewModel.deleteTransaction(transaction)

        advanceUntilIdle()

        assertEquals(0, transactionRepository.currentTransactions.size)
    }

    @Test
    fun `should cancel editing transaction`() = runTest {
        val category = Category(
            id = 1,
            name = "Alimentação",
            type = TransactionType.EXPENSE
        )

        val payment = PaymentMethod(
            id = 1,
            name = "Pix"
        )

        val transaction = Transaction(
            id =  1,
            title = "Mercado",
            amount = 25.0,
            type = TransactionType.EXPENSE,
            category = category,
            paymentMethod = payment,
            date = 123456789L
        )

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(category)
        )

        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(payment)
        )

        val transactionRepository = FakeTransactionRepository(
            initialTransactions = listOf(transaction)
        )

        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        viewModel.startEditing(transaction)

        advanceUntilIdle()

        assertEquals(transaction, viewModel.uiState.value.editingTransaction)
        assertEquals("Mercado", viewModel.uiState.value.description)
        assertEquals("2500", viewModel.uiState.value.amountDigits)
        assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.type)
        assertEquals(category, viewModel.uiState.value.selectedCategory)
        assertEquals(payment, viewModel.uiState.value.selectedPaymentMethod)
        assertEquals(123456789L, viewModel.uiState.value.dateMillis)

        viewModel.cancelEditing()

        assertNull(viewModel.uiState.value.editingTransaction)
        assertEquals("", viewModel.uiState.value.description)
        assertEquals("", viewModel.uiState.value.amountDigits)
        assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.type)
        assertNull(viewModel.uiState.value.error)

        assertEquals("Mercado", transactionRepository.currentTransactions.first().title)
        assertEquals(25.0, transactionRepository.currentTransactions.first().amount, 0.0)
    }

    @Test
    fun `should update transaction`() = runTest {
        val category1 = Category(
            id = 1,
            name = "Alimentação",
            type = TransactionType.EXPENSE
        )

        val category2 = Category(
            id = 2,
            name = "Salário",
            type = TransactionType.INCOME
        )

        val payment1 = PaymentMethod(
            id = 1,
            name = "Pix"
        )

        val payment2 = PaymentMethod(
            id = 2,
            name = "Dinheiro"
        )

        val transaction = Transaction(
            id =  1,
            title = "Mercado",
            amount = 25.0,
            type = TransactionType.EXPENSE,
            category = category1,
            paymentMethod = payment1,
            date = 123456789L
        )

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(category1, category2)
        )

        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(payment1, payment2)
        )

        val transactionRepository = FakeTransactionRepository(
            initialTransactions = listOf(transaction)
        )

        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle()
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        var wasSaved = false

        viewModel.startEditing(transaction)

        viewModel.onTypeChange(TransactionType.INCOME)
        advanceUntilIdle()

        viewModel.onAmountChange("250000") //2500.00
        viewModel.onDescriptionChange("Salario")
        viewModel.onCategoryChange(category2)
        viewModel.onPaymentMethodChange(payment2)
        viewModel.onDateChange(123456788L)
        viewModel.saveTransaction (
            onSaved = {
                wasSaved = true
            }
        )

        advanceUntilIdle()

        val updatedTransaction = transactionRepository.currentTransactions.first()

        assertEquals(1, transactionRepository.currentTransactions.size)
        assertEquals(1L, updatedTransaction.id)
        assertEquals("Salario", updatedTransaction.title)
        assertEquals(2500.0, updatedTransaction.amount, 0.0)
        assertEquals(category2, updatedTransaction.category)
        assertEquals(payment2, updatedTransaction.paymentMethod)
        assertEquals(123456788L, updatedTransaction.date)
        assertEquals(true, wasSaved)
    }

    @Test
    fun `should load transaction by id`() = runTest {
        val category = Category(
            id = 1,
            name = "Alimentação",
            type = TransactionType.EXPENSE
        )

        val payment = PaymentMethod(
            id = 1,
            name = "Pix"
        )

        val transaction = Transaction(
            id = 1,
            title = "Mercado",
            amount = 25.0,
            type = TransactionType.EXPENSE,
            category = category,
            paymentMethod = payment,
            date = 123456789L
        )

        val transactionRepository = FakeTransactionRepository(
            initialTransactions = listOf(transaction)
        )

        val categoryRepository = FakeCategoryRepository(
            initialCategories = listOf(category)
        )

        val paymentMethodRepository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(payment)
        )

        val viewModel = TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository,
            savedStateHandle = SavedStateHandle(
                mapOf("id" to 1L)
            )
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()

        assertEquals(transaction, viewModel.uiState.value.editingTransaction)
        assertEquals("Mercado", viewModel.uiState.value.description)
        assertEquals("2500", viewModel.uiState.value.amountDigits)
        assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.type)
        assertEquals(category, viewModel.uiState.value.selectedCategory)
        assertEquals(payment, viewModel.uiState.value.selectedPaymentMethod)
        assertEquals(123456789L, viewModel.uiState.value.dateMillis)
    }
}
