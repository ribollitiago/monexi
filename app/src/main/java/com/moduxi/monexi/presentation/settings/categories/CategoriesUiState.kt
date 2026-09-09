package com.moduxi.monexi.presentation.settings.categories

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType

data class CategoriesUiState (
    val categories: List<Category> = emptyList(),
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val newCategoryName: String = "",
    val editingCategory: Category? = null,
    val editingCategoryName: String = "",
    val error: String? = null
)