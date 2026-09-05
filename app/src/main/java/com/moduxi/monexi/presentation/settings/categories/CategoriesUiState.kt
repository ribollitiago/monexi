package com.moduxi.monexi.presentation.settings.categories

import com.moduxi.monexi.domain.model.Category

data class CategoriesUiState (
    val categories: List<Category> = emptyList(),
    val newCategoryName: String = "",
    val editingCategory: Category? = null,
    val editingCategoryName: String = "",
    val error: String? = null
)