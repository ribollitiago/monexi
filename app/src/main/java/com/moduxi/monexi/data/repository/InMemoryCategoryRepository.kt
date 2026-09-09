package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object InMemoryCategoryRepository : CategoryRepository{

    private val _categories = MutableStateFlow(
        listOf(
            Category(id = 1, name = "Alimentacao", type = TransactionType.EXPENSE, isDefault = true),
            Category(id = 2, name = "Transporte", type = TransactionType.EXPENSE, isDefault = true),
            Category(id = 3, name = "Casa", type = TransactionType.EXPENSE, isDefault = true),
            Category(id = 4, name = "Trabalho", type = TransactionType.EXPENSE, isDefault = true)
        )
    )

    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    override suspend fun addCategory(category: Category) {
        _categories.value += category
    }

    override suspend fun updateCategory(category: Category) {
        if (category.isDefault) return

        _categories.value = _categories.value.map { currentCategory ->
            if (currentCategory.id == category.id) category else currentCategory
        }
    }

    override suspend fun deleteCategory(category: Category) {
        if (category.isDefault) return

        _categories.value = _categories.value.filterNot { currentCategory ->
            currentCategory.id == category.id
        }
    }
}