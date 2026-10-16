package com.moduxi.monexi.presentation.settings.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val selectedTypeState = MutableStateFlow(TransactionType.EXPENSE)

    val uiState = combine(
        categoryRepository.categories,
        selectedTypeState
    ) { categories, selectedType ->
        CategoriesUiState(
            categories = categories,
            selectedType = selectedType
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState()
    )

    fun onTypeChange(type: TransactionType) {
        selectedTypeState.value = type
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication)
                CategoriesViewModel(
                    application.categoryRepository
                )
            }
        }
    }
}
