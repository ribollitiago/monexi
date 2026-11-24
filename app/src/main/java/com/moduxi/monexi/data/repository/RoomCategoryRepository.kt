package com.moduxi.monexi.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.dao.CategoryDao
import com.moduxi.monexi.data.local.entity.CategoryEntity
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.data.sync.RemoteStore
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class RoomCategoryRepository(
    private val categoryDao: CategoryDao,
    private val authRepository: AuthRepository,
    private val remoteStore: RemoteStore
) : CategoryRepository {

    /** Envia as categorias personalizadas do usuário (as padrão têm id fixo e não vão para a nuvem). */
    suspend fun pushAllLocal() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        categoryDao.observeAllCategoriesByUser(currentUserId).first()
            .filter { !it.isDefault && it.userId == currentUserId }
            .forEach { entity ->
                remoteStore.set(
                    "categories",
                    entity.id,
                    hashMapOf(
                        "id" to entity.id,
                        "userId" to currentUserId,
                        "name" to entity.name,
                        "type" to entity.type,
                        "isDefault" to false,
                        "isArchived" to entity.isArchived,
                        "colorHex" to entity.colorHex,
                        "iconName" to entity.iconName,
                        "monthlyBudget" to entity.monthlyBudget
                    )
                )
            }
    }

    override val categories: Flow<List<Category>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return categoryDao.observeCategoriesByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override val allCategories: Flow<List<Category>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return categoryDao.observeAllCategoriesByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override val archivedCategories: Flow<List<Category>>
        get() {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            return categoryDao.observeArchivedCategoriesByUser(currentUserId).map { list ->
                list.map { it.toDomain() }
            }
        }

    override suspend fun addCategory(category: Category) {
        val currentUserId = authRepository.currentUser?.uid ?: ""
        if (currentUserId.isEmpty()) {
            Log.e("FirestoreSync", "Erro ao adicionar categoria: Usuário não logado!")
            return
        }

        val categoryId = if (category.id == "0" || category.id.isEmpty()) {
            UUID.randomUUID().toString()
        } else {
            category.id
        }

        val entity = category.copy(id = categoryId, userId = currentUserId).toEntity()

        categoryDao.insertCategory(entity)

        val categoryData = hashMapOf(
            "id" to categoryId,
            "userId" to currentUserId,
            "name" to category.name,
            "type" to category.type.name,
            "isDefault" to false,
            "isArchived" to category.isArchived,
            "colorHex" to category.colorHex,
            "iconName" to category.iconName,
            "monthlyBudget" to category.monthlyBudget
        )

        remoteStore.set("categories", categoryId, categoryData)
    }

    override suspend fun updateCategory(category: Category){
        if (category.isDefault) return

        val currentUserId = authRepository.currentUser?.uid ?: return

        categoryDao.updateCategory(category.toEntity())

        val categoryData = hashMapOf(
            "id" to category.id,
            "userId" to category.userId,
            "name" to category.name,
            "type" to category.type.name,
            "isDefault" to false,
            "isArchived" to category.isArchived,
            "colorHex" to category.colorHex,
            "iconName" to category.iconName,
            "monthlyBudget" to category.monthlyBudget
        )

        remoteStore.set("categories", category.id.toString(), categoryData)
    }

    override suspend fun deleteCategory(category: Category) {
        if (category.isDefault) return

        val archivedCategory = category.copy(isArchived = true)

        categoryDao.updateCategory(archivedCategory.toEntity())

        val currentUserId = authRepository.currentUser?.uid ?: return

        val categoryData = hashMapOf(
            "id" to archivedCategory.id,
            "userId" to archivedCategory.userId,
            "name" to archivedCategory.name,
            "type" to archivedCategory.type.name,
            "isDefault" to archivedCategory.isDefault,
            "isArchived" to true,
            "colorHex" to archivedCategory.colorHex,
            "iconName" to archivedCategory.iconName,
            "monthlyBudget" to archivedCategory.monthlyBudget
        )

        remoteStore.set("categories", archivedCategory.id, categoryData)
    }

    override suspend fun getCategoryById(id: String): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun syncFromRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUserId = authRepository.currentUser?.uid
            ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("categories")
                .get()
                .await()

            val remoteCategories = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val typeStr = doc.getString("type") ?: "EXPENSE"
                val type = try {
                    TransactionType.valueOf(typeStr)
                } catch (e: Exception) {
                    TransactionType.EXPENSE
                }
                val isDefault = doc.getBoolean("isDefault") ?: false
                val categoryId = doc.getString("id") ?: doc.id
                val isArchived = doc.getBoolean("isArchived") ?: false
                val colorHex = doc.getString("colorHex") ?: "#1B8A5A"
                val iconName = doc.getString("iconName") ?: "Category"
                val monthlyBudget = (doc.get("monthlyBudget") as? Number)?.toDouble() ?: 0.0

                CategoryEntity(
                    id = categoryId,
                    userId = currentUserId,
                    name = name,
                    type = type.name,
                    isDefault = isDefault,
                    isArchived = isArchived,
                    colorHex = colorHex,
                    iconName = iconName,
                    monthlyBudget = monthlyBudget
                )
            }

            remoteCategories.forEach { entity ->
                if (remoteStore.isPending("categories", entity.id)) return@forEach
                categoryDao.insertCategory(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
