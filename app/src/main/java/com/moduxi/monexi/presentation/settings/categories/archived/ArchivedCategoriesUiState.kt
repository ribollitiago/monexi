package com.moduxi.monexi.presentation.settings.categories.archived

import com.moduxi.monexi.domain.model.Category

data class ArchivedCategoriesUiState(
    val categoriesArchived: List<Category> = emptyList(),
    val error: String? = null
)