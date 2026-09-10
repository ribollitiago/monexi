package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCategoryRepository(
    initialCategories: List<Category> = emptyList()
) : CategoryRepository {

    private val categoriesState = MutableStateFlow(initialCategories)

    override val categories: Flow<List<Category>> = categoriesState

    override suspend fun addCategory(category: Category) {
        categoriesState.value += category.copy(
                    id = if (category.id == 0L) categoriesState.value.size + 1L else category.id
                )
    }

    override suspend fun updateCategory(category: Category) {
        categoriesState.value = categoriesState.value.map {
            if (it.id == category.id) category else it
        }
    }

    override suspend fun deleteCategory(category: Category) {
        categoriesState.value = categoriesState.value.filterNot {
            it.id == category.id
        }
    }
}