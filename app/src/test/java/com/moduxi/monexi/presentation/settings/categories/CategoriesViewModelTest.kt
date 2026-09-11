package com.moduxi.monexi.presentation.settings.categories

import com.moduxi.monexi.MainDispatcherRule
import com.moduxi.monexi.data.repository.FakeCategoryRepository
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.CategoryRepository
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
class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
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

    @Test
    fun `should not add blank category`() = runTest {
        val repository = FakeCategoryRepository()
        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.onTypeChange(TransactionType.INCOME)
        viewModel.onNewCategoryNameChange("")
        viewModel.addCategory()

        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.categories.size)
        assertEquals("Informe o nome da categoria", viewModel.uiState.value.error)
    }

    @Test
    fun `should edit custom category`() = runTest {
        val category = Category(
            id = 1,
            name = "Outros",
            type = TransactionType.INCOME,
            isDefault = false
        )

        val repository = FakeCategoryRepository(
            initialCategories = listOf(category)
        )

        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.startEditing(category)
        viewModel.onEditingCategoryNameChange("Alimentação")
        viewModel.saveEditing()

        advanceUntilIdle()

        //Valida se saiu da edição
        assertEquals(null, viewModel.uiState.value.editingCategory)
        assertEquals("", viewModel.uiState.value.editingCategoryName)

        //Valida a alteração
        assertTrue(
            viewModel.uiState.value.categories.any {
                it.name == "Alimentação" && it.type == TransactionType.INCOME
            }
        )
    }

    @Test
    fun `should not edit default category`() = runTest {
        val category = Category(
            id = 1,
            name = "Alimentação",
            type = TransactionType.INCOME,
            isDefault = true
        )

        val repository = FakeCategoryRepository(
            initialCategories = listOf(category)
        )

        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.startEditing(category)

        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.editingCategory)
        assertEquals("", viewModel.uiState.value.editingCategoryName)
    }

    @Test
    fun `should delete custom category`() = runTest {
        val category = Category(
            id = 1,
            name = "Alimentação",
            type = TransactionType.EXPENSE,
            isDefault = false
        )
        val repository = FakeCategoryRepository(
            initialCategories = listOf(category)
        )

        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.deleteCategory(category)

        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.categories.size)
    }

    @Test
    fun `should not delete default category`() = runTest {
        val category = Category(
            id = 1,
            name = "Alimentação",
            type = TransactionType.EXPENSE,
            isDefault = true
        )
        val repository = FakeCategoryRepository(
            initialCategories = listOf(category)
        )

        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.deleteCategory(category)

        advanceUntilIdle()

        assertEquals("Alimentação", viewModel.uiState.value.categories.first().name)
        assertEquals(1, viewModel.uiState.value.categories.size)
    }

    @Test
    fun `should clear error on typing a new name`() = runTest {
        val repository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Outros",
                    type = TransactionType.EXPENSE
                )
            )
        )
        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.onNewCategoryNameChange("Outros")
        viewModel.addCategory()

        assertEquals("Categoria ja existe", viewModel.uiState.value.error)

        viewModel.onNewCategoryNameChange("Viagem")

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `should add category after duplicated category error`() = runTest {
        val repository = FakeCategoryRepository(
            initialCategories = listOf(
                Category(
                    id = 1,
                    name = "Outros",
                    type = TransactionType.EXPENSE
                )
            )
        )
        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.onNewCategoryNameChange("Outros")
        viewModel.addCategory()

        assertEquals("Categoria ja existe", viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.categories.size)

        viewModel.onNewCategoryNameChange("Viagem")
        viewModel.addCategory()

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(2, viewModel.uiState.value.categories.size)
    }

    @Test
    fun `should cancel editing category`() = runTest {
        val category = Category(
            id = 1,
            name = "Alimentação",
            type = TransactionType.EXPENSE,
            isDefault = false
        )
        val repository = FakeCategoryRepository(
            initialCategories = listOf(category)
        )

        val viewModel = CategoriesViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect{}
        }

        viewModel.startEditing(category)
        viewModel.onEditingCategoryNameChange("Teste")

        assertEquals("Teste", viewModel.uiState.value.editingCategoryName)

        viewModel.cancelEditing()

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.editingCategory)
        assertEquals("", viewModel.uiState.value.editingCategoryName)
        assertEquals("Alimentação", viewModel.uiState.value.categories.first().name)
    }
}