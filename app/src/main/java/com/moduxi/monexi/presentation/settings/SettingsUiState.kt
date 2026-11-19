package com.moduxi.monexi.presentation.settings

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val userEmail: String? = null
)
