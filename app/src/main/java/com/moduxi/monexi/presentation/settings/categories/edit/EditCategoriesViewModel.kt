package com.moduxi.monexi.presentation.settings.categories.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.transaction.TransactionType
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

    fun onBudgetDigitsChange(value: String) {
        _uiState.value = _uiState.value.copy(
            budgetDigits = value.filter { it.isDigit() }.take(12),
            error = null
        )
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            error = null
        )
    }

    fun onColorChange(colorHex: String) {
        _uiState.value = _uiState.value.copy(
            selectedColorHex = colorHex,
            error = null
        )
    }

    fun onIconChange(iconName: String) {
        _uiState.value = _uiState.value.copy(
            selectedIconName = iconName,
            error = null
        )
    }

    fun loadCategory(id: String) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(id)
            category?.let {
                val budgetCents = Math.round(it.monthlyBudget * 100)
                val budgetStr = if (budgetCents > 0) budgetCents.toString() else ""

                _uiState.value = _uiState.value.copy(
                    editingCategory = it,
                    categoryName = it.name,
                    budgetDigits = budgetStr,
                    selectedType = it.type,
                    selectedColorHex = it.colorHex,
                    selectedIconName = it.iconName
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

        val budgetValue = (state.budgetDigits.toLongOrNull() ?: 0L) / 100.0

        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: ""
            val editingCategory = state.editingCategory

            if (editingCategory == null) {
                categoryRepository.addCategory(
                    Category(
                        id = "0",
                        userId = currentUserId,
                        name = name,
                        type = state.selectedType,
                        isDefault = false,
                        colorHex = state.selectedColorHex,
                        iconName = state.selectedIconName,
                        monthlyBudget = budgetValue
                    )
                )
            } else {
                categoryRepository.updateCategory(
                    editingCategory.copy(
                        name = name,
                        type = state.selectedType,
                        colorHex = state.selectedColorHex,
                        iconName = state.selectedIconName,
                        monthlyBudget = budgetValue
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
