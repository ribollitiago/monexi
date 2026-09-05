package com.moduxi.monexi.presentation.settings.categories

import com.moduxi.monexi.domain.model.Category

class CategoriesUiState (
    val categories: List<Category> = emptyList(),
    val newCategoryName: String = "",
    val error: String? = null
)