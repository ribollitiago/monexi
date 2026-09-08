package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CategoryRepository {
    val categories: Flow<List<Category>>

    suspend fun addCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
}