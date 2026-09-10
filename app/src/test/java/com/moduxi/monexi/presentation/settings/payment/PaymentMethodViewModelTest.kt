package com.moduxi.monexi.presentation.settings.payment

import com.moduxi.monexi.MainDispatcherRule
import com.moduxi.monexi.data.repository.FakePaymentMethodRepository
import com.moduxi.monexi.domain.model.PaymentMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentMethodViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun`should add payment method`() = runTest {
        val repository = FakePaymentMethodRepository()
        val viewModel = PaymentMethodViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.onNewPaymentMethodNameChange("Pix")
        viewModel.addPaymentMethod()

        advanceUntilIdle()

        val paymentMethods = viewModel.uiState.value.paymentMethods

        assertEquals(1, paymentMethods.size)
        assertEquals("Pix", paymentMethods.first().name)
    }

    @Test
    fun `should not add duplicated payment method`() = runTest {
        val repository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                PaymentMethod(
                    id = 1,
                    name = "Pix"
                )
            )
        )
        val viewModel = PaymentMethodViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.onNewPaymentMethodNameChange("Pix")
        viewModel.addPaymentMethod()

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.paymentMethods.size)
        assertEquals("Método de pagamento já existe", viewModel.uiState.value.error)
    }

    @Test
    fun `should clear error when typing a new name`() = runTest {
        val repository = FakePaymentMethodRepository()
        val viewModel = PaymentMethodViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.addPaymentMethod()
        assertEquals("Informe o nome do método de pagamento", viewModel.uiState.value.error)

        viewModel.onNewPaymentMethodNameChange("Cartão")

        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `should not add blank payment method`() = runTest {
        val repository = FakePaymentMethodRepository()
        val viewModel = PaymentMethodViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.addPaymentMethod()

        advanceUntilIdle()

        assertEquals("Informe o nome do método de pagamento", viewModel.uiState.value.error)
        assertEquals(0, viewModel.uiState.value.paymentMethods.size)
    }

    @Test
    fun `should edit custom payment method`() = runTest {
        val paymentMethod = PaymentMethod(
            id = 1,
            name = "Pix",
            isDefault = false
        )

        val repository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(paymentMethod)
        )

        val viewModel = PaymentMethodViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.startEditing(paymentMethod)
        viewModel.onEditingPaymentMethodChange("Débito")
        viewModel.saveEditing()

        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.editingPaymentMethod)
        assertEquals("", viewModel.uiState.value.editingPaymentMethodName)
        assertEquals(1, viewModel.uiState.value.paymentMethods.size)
        assertEquals("Débito", viewModel.uiState.value.paymentMethods.first().name)
    }

    @Test
    fun `should not edit default payment method`() = runTest {
        val paymentMethod = PaymentMethod(
            id = 1,
            name = "Pix",
            isDefault = true
        )

        val repository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                paymentMethod
            )
        )

        val viewModel = PaymentMethodViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.startEditing(paymentMethod)

        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.editingPaymentMethod)
        assertEquals("", viewModel.uiState.value.editingPaymentMethodName)
    }

    @Test
    fun `should delete custom payment method`() = runTest {
        val paymentMethod = PaymentMethod(
            id = 1,
            name = "Pix",
            isDefault = false
        )

        val repository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                paymentMethod
            )
        )

        val viewModel = PaymentMethodViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        assertEquals(1, viewModel.uiState.value.paymentMethods.size)

        viewModel.deletePaymentMethod(paymentMethod)

        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.paymentMethods.size)
        assertTrue(
            viewModel.uiState.value.paymentMethods.none {
                it.id == paymentMethod.id
            }
        )
    }

    @Test
    fun `should not delete default payment method`() = runTest {
        val paymentMethod = PaymentMethod(
            id = 1,
            name = "Pix",
            isDefault = true
        )

        val repository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                paymentMethod
            )
        )

        val viewModel = PaymentMethodViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        assertEquals(1, viewModel.uiState.value.paymentMethods.size)

        viewModel.deletePaymentMethod(paymentMethod)

        advanceUntilIdle()

        assertEquals("Pix", viewModel.uiState.value.paymentMethods.first().name)
        assertEquals(1, viewModel.uiState.value.paymentMethods.size)
    }

    @Test
    fun `should cancel editing payment method`() = runTest {
        val paymentMethod = PaymentMethod(
            id = 1,
            name = "Pix",
            isDefault = false
        )

        val repository = FakePaymentMethodRepository(
            initialPaymentMethods = listOf(
                paymentMethod
            )
        )

        val viewModel = PaymentMethodViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.startEditing(paymentMethod)
        viewModel.onEditingPaymentMethodChange("Teste")

        assertEquals("Teste", viewModel.uiState.value.editingPaymentMethodName)

        viewModel.cancelEditing()

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.editingPaymentMethod)
        assertEquals("", viewModel.uiState.value.editingPaymentMethodName)
        assertEquals("Pix", viewModel.uiState.value.paymentMethods.first().name)
    }
}