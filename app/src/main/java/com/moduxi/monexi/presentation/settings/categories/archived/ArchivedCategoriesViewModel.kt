package com.moduxi.monexi.presentation.settings.categories.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchivedCategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<ArchivedCategoriesUiState> = categoryRepository.archivedCategories
        .map { categories ->
            ArchivedCategoriesUiState(
                categoriesArchived = categories
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArchivedCategoriesUiState()
        )

    fun unarchivedClick(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category.copy(isArchived = false))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                ArchivedCategoriesViewModel(
                    application.categoryRepository
                )
            }
        }
    }
}