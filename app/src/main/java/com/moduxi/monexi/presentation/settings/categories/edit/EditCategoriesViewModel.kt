package com.moduxi.monexi.presentation.settings.categories.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditCategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditCategoriesUiState())
    val uiState: StateFlow<EditCategoriesUiState> = _uiState.asStateFlow()

    init {
        val categoryId: String? = savedStateHandle.get<String>("id")
        if (!categoryId.isNullOrEmpty()) {
            loadCategory(categoryId)
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            categoryName = name,
            error = null
        )
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            error = null
        )
    }

    fun loadCategory(id: String) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(id)
            category?.let {
                _uiState.value = _uiState.value.copy(
                    editingCategory = it,
                    categoryName = it.name,
                    selectedType = it.type
                )
            }
        }
    }

    fun saveCategory(onSaved: () -> Unit) {
        val state = _uiState.value
        val name = state.categoryName.trim()

        if (name.isBlank()) {
            _uiState.value = state.copy(error = "Informe o nome da categoria")
            return
        }

        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            val editingCategory = state.editingCategory

            if (editingCategory == null) {
                // Criando nova categoria
                categoryRepository.addCategory(
                    Category(
                        id = "0",
                        userId = currentUserId,
                        name = name,
                        type = state.selectedType,
                        isDefault = false
                    )
                )
            } else {
                // Editando categoria existente
                categoryRepository.updateCategory(
                    editingCategory.copy(
                        name = name,
                        type = state.selectedType
                    )
                )
            }
            onSaved()
        }
    }

    fun deleteCategory(onDeleted: () -> Unit) {
        val category = _uiState.value.editingCategory ?: return
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
            onDeleted()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                val savedStateHandle = createSavedStateHandle()
                EditCategoriesViewModel(
                    categoryRepository = application.categoryRepository,
                    authRepository = application.authRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
