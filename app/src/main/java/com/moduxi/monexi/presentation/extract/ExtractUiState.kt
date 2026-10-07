package com.moduxi.monexi.presentation.extract

import com.moduxi.monexi.domain.model.Transaction

data class ExtractUiState (
    val search: String = "",
    val transactions: List<Transaction> = emptyList()
)