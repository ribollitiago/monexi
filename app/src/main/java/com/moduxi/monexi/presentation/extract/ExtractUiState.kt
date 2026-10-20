package com.moduxi.monexi.presentation.extract

import com.moduxi.monexi.domain.model.Transaction

data class ExtractUiState(
    val search: String = "",
    val selectedMonthName: String = "",
    val transactions: List<Transaction> = emptyList()
)