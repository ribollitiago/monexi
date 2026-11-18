package com.moduxi.monexi.presentation.settings.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.data.repository.GithubIssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportIssueViewModel(
    private val githubIssueRepository: GithubIssueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportIssueUiState())
    val uiState = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        if (value.length <= 256) {
            _uiState.update { it.copy(title = value, errorMessage = null) }
        }
    }

    fun onBodyChange(value: String) {
        if (value.length <= 5000) {
            _uiState.update { it.copy(body = value, errorMessage = null) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun submit() {
        val current = _uiState.value
        if (current.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe um título para o problema.") }
            return
        }
        if (current.isSending) return

        _uiState.update { it.copy(isSending = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = githubIssueRepository.createIssue(
                title = current.title.trim(),
                body = current.body.trim().ifBlank { null }
            )

            result.onSuccess { issue ->
                _uiState.update {
                    ReportIssueUiState(
                        successMessage = "Problema #${issue.number} enviado com sucesso!"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = error.message ?: "Erro desconhecido ao enviar."
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonexiApplication
                ReportIssueViewModel(
                    githubIssueRepository = GithubIssueRepository(
                        authRepository = app.authRepository
                    )
                )
            }
        }
    }
}
