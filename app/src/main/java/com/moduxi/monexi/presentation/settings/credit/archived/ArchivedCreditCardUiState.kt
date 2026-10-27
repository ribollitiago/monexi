package com.moduxi.monexi.presentation.settings.credit.archived

import com.moduxi.monexi.domain.model.CreditCard

data class ArchivedCreditCardUiState(
    val creditCardsArchived: List<CreditCard> = emptyList(),
    val error: String? = null
)
