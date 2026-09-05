package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.Category
import kotlinx.coroutines.flow.StateFlow

interface CategoryRepository {
    val categories: StateFlow<List<Category>>

    fun addCategory(category: Category)
    fun deleteCategory(category: Category)
}