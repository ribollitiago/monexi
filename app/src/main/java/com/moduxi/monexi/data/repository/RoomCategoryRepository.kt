package com.moduxi.monexi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.data.local.dao.CategoryDao
import com.moduxi.monexi.data.local.entity.CategoryEntity
import com.moduxi.monexi.data.local.mapper.toDomain
import com.moduxi.monexi.data.local.mapper.toEntity
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

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
        if (currentUserId.isEmpty()) {
            android.util.Log.e("FirestoreSync", "Erro ao adicionar categoria: Usuário não logado!")
            return
        }

        val categoryId = if (category.id == "0" || category.id.isEmpty()) {
            java.util.UUID.randomUUID().toString()
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
            "isDefault" to false
        )

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("categories")
                .document(categoryId)
                .set(categoryData)
                .await()

            android.util.Log.d("FirestoreSync", "Categoria $categoryId salva no Firestore com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("FirestoreSync", "Erro ao salvar categoria no Firestore: ${e.message}", e)
        }
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
            "isDefault" to false
        )

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("categories")
                .document(category.id.toString())
                .set(categoryData)
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteCategory(category: Category) {
        if (category.isDefault) return

        categoryDao.deleteCategory(category.toEntity())

        val currentUserId = authRepository.currentUser?.uid ?: return

        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("categories")
                .document(category.id.toString())
                .delete()
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun syncFromRemote(): Result<Unit> {
        val currentUserId = authRepository.currentUser?.uid
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("categories")
                .get()
                .await()

            val remoteCategories = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val typeStr = doc.getString("type") ?: "EXPENSE"
                val type = TransactionType.valueOf(typeStr)
                val isDefault = doc.getBoolean("isDefault") ?: false
                val categoryId = doc.getString("id") ?: doc.id

                CategoryEntity(
                    id = categoryId,
                    userId = currentUserId,
                    name = name,
                    type = type.name,
                    isDefault = isDefault
                )
            }

            remoteCategories.forEach { entity ->
                categoryDao.insertCategory(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}