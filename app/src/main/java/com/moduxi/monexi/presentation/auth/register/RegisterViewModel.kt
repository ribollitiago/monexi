package com.moduxi.monexi.presentation.auth.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.presentation.auth.login.LoginUiState
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onNameChange(newName: String) {
        uiState = uiState.copy(name = newName)
    }

    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String){
        uiState = uiState.copy(password = newPassword)
    }

    fun onPasswordConfirmChange(newConfirmPassword: String){
        uiState = uiState.copy(confirmPassword = newConfirmPassword)
    }

    fun onRegisterClick() {
        if (uiState.name.isBlank() ||
            uiState.email.isBlank() ||
            uiState.password.isBlank() ||
            uiState.confirmPassword.isBlank()
        ){
            uiState = uiState.copy(errorMessage = "Preencha todos os campos.")
            return
        }

        if (uiState.password != uiState.confirmPassword) {
            uiState = uiState.copy(errorMessage = "As senhas não coincidem.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val result = authRepository.signUp(uiState.email, uiState.password)

            result.onSuccess {
                uiState = uiState.copy(
                    isLoading = false,
                    isSuccess = true
                )
            }.onFailure { exception ->
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = exception.localizedMessage ?: "Falha ao realizar registro."
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication
                RegisterViewModel(application.authRepository)
            }
        }
    }
}