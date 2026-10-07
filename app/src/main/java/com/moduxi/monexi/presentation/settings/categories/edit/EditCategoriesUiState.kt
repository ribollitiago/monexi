package com.moduxi.monexi.presentation.settings.categories.edit

import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType

data class EditCategoriesUiState(
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val categoryName: String = "",
    val editingCategory: Category? = null,
    val error: String? = null
)
