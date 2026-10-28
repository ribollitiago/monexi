package com.moduxi.monexi.presentation.settings.categories

import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.transaction.TransactionType

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val error: String? = null
)
