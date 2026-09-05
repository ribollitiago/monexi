package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object InMemoryCategoryRepository : CategoryRepository{

    private val _categories = MutableStateFlow(
        listOf(
            Category(id = 1, name = "Alimentacao", isDefault = true),
            Category(id = 2, name = "Transporte", isDefault = true),
            Category(id = 3, name = "Casa", isDefault = true),
            Category(id = 4, name = "Trabalho", isDefault = true)
        )
    )

    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    override fun addCategory(category: Category) {
        _categories.value += category
    }

    override fun deleteCategory(category: Category) {
        _categories.value -= category
    }
}