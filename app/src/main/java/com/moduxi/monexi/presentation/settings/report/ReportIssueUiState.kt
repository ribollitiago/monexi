package com.moduxi.monexi.presentation.settings.report

data class ReportIssueUiState(
    val title: String = "",
    val body: String = "",
    val isSending: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
