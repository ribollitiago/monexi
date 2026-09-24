package com.moduxi.monexi.data.repository

import com.moduxi.monexi.data.local.dao.CategoryDao
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository(
    private val categoryDao: CategoryDao,
    private val authRepository: AuthRepository
) : CategoryRepository {

    override val categories: Flow<List<Category>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return categoryDao.observeCategoriesByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override suspend fun addCategory(category: Category) {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        val entity = category.copy(userId = currentUserId).toEntity()
        categoryDao.insertCategory(entity)
    }

    override suspend fun updateCategory(category: Category){
        if (category.isDefault) return

        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(category: Category) {
        if (category.isDefault) return

        categoryDao.deleteCategory(category.toEntity())
    }
}