package com.moduxi.monexi.domain.repository

import com.moduxi.monexi.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    val categories: Flow<List<Category>>
    val allCategories: Flow<List<Category>>
    val archivedCategories: Flow<List<Category>>

    suspend fun addCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun getCategoryById(id: String): Category?
    suspend fun syncFromRemote(): Result<Unit>
}