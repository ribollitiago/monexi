package com.moduxi.monexi.presentation.settings.categories

import com.moduxi.monexi.MainDispatcherRule
import com.moduxi.monexi.data.repository.FakeCategoryRepository
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `should add category`() = runTest {
        val repository = FakeCategoryRepository()
        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.onNewCategoryNameChange("Viagem")
        viewModel.addCategory()

        advanceUntilIdle()

        val categories = viewModel.uiState.value.categories

        assertEquals(1, categories.size)
        assertEquals("Viagem", categories.first().name)
        assertEquals(TransactionType.EXPENSE, categories.first().type)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `should not add duplicated category for same type`() = runTest {
        val repository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Viagem",
                    type = TransactionType.EXPENSE
                )
            )
        )
        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.onNewCategoryNameChange("Viagem")
        viewModel.addCategory()

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.categories.size)
        assertEquals("Categoria ja existe", viewModel.uiState.value.error)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `should allow same category name for different type`() = runTest {
        val repository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Outros",
                    type = TransactionType.INCOME
                )
            )
        )

        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.onTypeChange(TransactionType.EXPENSE)
        viewModel.onNewCategoryNameChange("Outros")
        viewModel.addCategory()

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.categories.size)
        assertTrue(
            viewModel.uiState.value.categories.any {
                it.name == "Outros" && it.type == TransactionType.EXPENSE
            }
        )
    }
}