package com.moduxi.monexi.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.data.repository.local.PrivacyManager
import com.moduxi.monexi.data.repository.local.ThemeManager
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeManager: ThemeManager,
    private val privacyManager: PrivacyManager,
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState = combine(
        themeManager.isDarkMode,
        privacyManager.isBiometricEnabled
    ) { isDark, isBiometric ->
        SettingsUiState(
            isDarkTheme = isDark,
            isBiometricEnabled = isBiometric,
            userEmail = authRepository.currentUser?.email
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(isDark)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            privacyManager.setBiometricEnabled(enabled)
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            transactionRepository.clearLocalData()
            authRepository.signOut()
            onLogoutSuccess()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication
                SettingsViewModel(
                    themeManager = application.themeManager,
                    privacyManager = application.privacyManager,
                    authRepository = application.authRepository,
                    transactionRepository = application.transactionRepository
                )
            }
        }
    }
}
