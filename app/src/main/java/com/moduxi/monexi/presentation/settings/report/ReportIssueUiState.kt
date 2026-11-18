package com.moduxi.monexi.presentation.settings.report

data class ReportIssueUiState(
    val type: ReportType = ReportType.BUG,
    val title: String = "",
    val body: String = "",
    val isSending: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
