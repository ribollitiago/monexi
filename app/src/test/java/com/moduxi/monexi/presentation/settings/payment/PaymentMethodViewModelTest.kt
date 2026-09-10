package com.moduxi.monexi.presentation.settings.payment

import com.moduxi.monexi.MainDispatcherRule
import com.moduxi.monexi.data.repository.FakePaymentMethodRepository
import com.moduxi.monexi.domain.model.PaymentMethod
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PaymentMethodViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
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
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `should not add duplicated payment method for same type`() = runTest {
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
    @OptIn(ExperimentalCoroutinesApi::class)
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
}