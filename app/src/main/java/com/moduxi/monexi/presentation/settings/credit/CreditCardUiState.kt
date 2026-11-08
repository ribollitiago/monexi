package com.moduxi.monexi.presentation.settings.credit

import com.moduxi.monexi.domain.model.creditcard.CreditCard

data class CreditCardUiState (
    val creditCards: List<CreditCard> = emptyList(),
    val error: String? = null
)