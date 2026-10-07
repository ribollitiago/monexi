package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.map

class FakeCategoryRepository(
    initialCategories: List<Category> = emptyList()
) : CategoryRepository {

    private val categoriesState = MutableStateFlow(initialCategories)

    override val categories: Flow<List<Category>> = categoriesState
    override val allCategories: Flow<List<Category>> = categoriesState
    override val archivedCategories: Flow<List<Category>>
        get() = categoriesState.map { list -> list.filter { it.isArchived } }

    override suspend fun addCategory(category: Category) {
        categoriesState.value += category.copy(
            id = if (category.id == "0" || category.id.isEmpty()) java.util.UUID.randomUUID().toString() else category.id
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

    override suspend fun getCategoryById(id: String): Category? {
        return categoriesState.value.firstOrNull { it.id == id }
    }

    override suspend fun syncFromRemote(): Result<Unit> {
        return Result.success(Unit)
    }
}
