package com.moduxi.monexi.presentation.settings.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moduxi.monexi.data.repository.InMemoryCategoryRepository
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository = InMemoryCategoryRepository
) : ViewModel() {

    private val formState = MutableStateFlow(CategoriesUiState())

    val uiState = combine(
        categoryRepository.categories,
        formState
    ) { categories, form ->
        form.copy(
            categories = categories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(categories = categoryRepository.categories.value)
    )

    fun onNewCategoryNameChange(name: String) {
        formState.value = formState.value.copy(
            newCategoryName = name,
            error = null
        )
    }

    fun addCategory() {
        val state = formState.value
        val name = state.newCategoryName.trim()

        if (name.isBlank()) {
            formState.value = state.copy(error = "Informe o nome da categoria")
            return
        }

        val alreadyExists = categoryRepository.categories.value.any { category ->
            category.name.equals(name, ignoreCase = true)
        }

        if (alreadyExists) {
            formState.value = state.copy(error = "Categoria ja existe")
            return
        }

        categoryRepository.addCategory(
            Category(
                id = System.currentTimeMillis(),
                name = name,
                isDefault = false
            )
        )

        formState.value = state.copy(
            newCategoryName = "",
            error = null
        )
    }

    fun startEditing(category: Category) {
        if (category.isDefault) return

        formState.value = formState.value.copy(
            editingCategory = category,
            editingCategoryName = category.name,
            error = null
        )
    }

    fun onEditingCategoryNameChange(name: String) {
        formState.value = formState.value.copy(
            editingCategoryName = name,
            error = null
        )
    }

    fun saveEditing() {
        val state = formState.value
        val category = state.editingCategory ?: return
        val name = state.editingCategoryName.trim()

        if (name.isBlank()) {
            formState.value = state.copy(error = "Informe o nome da categoria")
            return
        }

        val alreadyExists = categoryRepository.categories.value.any { currentCategory ->
            currentCategory.id != category.id &&
                    currentCategory.name.equals(name, ignoreCase = true)
        }

        if (alreadyExists) {
            formState.value = state.copy(error = "Categoria ja existe")
            return
        }

        categoryRepository.updateCategory(
            category.copy(name = name)
        )

        formState.value = state.copy(
            editingCategory = null,
            editingCategoryName = "",
            error = null
        )
    }

    fun cancelEditing() {
        formState.value = formState.value.copy(
            editingCategory = null,
            editingCategoryName = "",
            error = null
        )
    }

    fun deleteCategory(category: Category) {
        if (category.isDefault) return

        categoryRepository.deleteCategory(category)
    }
}