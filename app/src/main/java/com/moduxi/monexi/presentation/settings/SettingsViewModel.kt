package com.moduxi.monexi.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moduxi.monexi.data.repository.local.ThemeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val themeManager: ThemeManager) : ViewModel() {

    val uiState = themeManager.isDarkMode.map { isDark ->
        SettingsUiState(isDarkTheme = isDark)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(isDark)
        }
    }

    fun logout(){
        println("Logout")
    }
}