package com.moduxi.monexi.presentation.transaction

class TransactionUiState (
    val typeTransaction: String = "",
    val description: String = "",
    val value: Double = 0.0,
    val data: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
){
}