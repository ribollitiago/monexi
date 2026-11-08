package com.moduxi.monexi.presentation.settings.categories.edit

import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.transaction.TransactionType

data class EditCategoriesUiState(
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val categoryName: String = "",
    val selectedColorHex: String = "#1B8A5A",
    val selectedIconName: String = "Category",
    val editingCategory: Category? = null,
    val error: String? = null
)
