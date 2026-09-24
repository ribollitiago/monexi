package com.moduxi.monexi.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.data.repository.local.ThemeManager
import com.moduxi.monexi.domain.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.onSuccess

class SettingsViewModel(
    private val themeManager: ThemeManager,
    private val authRepository: AuthRepository) : ViewModel() {

    val uiState = themeManager.isDarkMode.map { isDark ->
        SettingsUiState(
            isDarkTheme = isDark,
            userEmail = authRepository.currentUser?.email)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(isDark)
        }
    }

    fun logout(onLogoutSuccess: () -> Unit){
        authRepository.signOut()
        onLogoutSuccess()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication
                SettingsViewModel(
                    themeManager = application.themeManager,
                    authRepository = application.authRepository
                )
            }
        }
    }
}