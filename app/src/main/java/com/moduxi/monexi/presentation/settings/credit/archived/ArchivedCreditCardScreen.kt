package com.moduxi.monexi.presentation.settings.credit.archived

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.CreditCard
import com.moduxi.monexi.ui.theme.MonexiTheme
import com.moduxi.monexi.ui.theme.toCategoryIcon
import com.moduxi.monexi.ui.theme.toComposeColor

@Composable
fun ArchivedCreditCardScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchivedCreditCardViewModel = viewModel(factory = ArchivedCreditCardViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    ArchivedCreditCardContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUnarchiveCreditCardClick = viewModel::unarchiveCreditCard,
        modifier = modifier
    )
}

@Composable
fun ArchivedCreditCardContent(
    uiState: ArchivedCreditCardUiState,
    onNavigateBack: () -> Unit,
    onUnarchiveCreditCardClick: (CreditCard) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }

            Text(
                text = "Cartões Arquivados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        if (uiState.creditCardsArchived.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum cartão de crédito arquivado.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.creditCardsArchived, key = { it.id }) { creditCard ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val cardColor = creditCard.colorHex.toComposeColor()

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(cardColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = creditCard.iconName.toCategoryIcon(),
                                    contentDescription = creditCard.name,
                                    tint = cardColor
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = creditCard.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "Fecha dia ${creditCard.closingDay} • Vence dia ${creditCard.dueDay}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onUnarchiveCreditCardClick(creditCard) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Unarchive,
                                    contentDescription = "Desarquivar"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchivedCreditCardScreenPreview() {
    MonexiTheme {
        ArchivedCreditCardContent(
            uiState = ArchivedCreditCardUiState(
                creditCardsArchived = listOf(
                    CreditCard(id = "1", userId = "1", name = "Nubank", closingDay = 5, dueDay = 12),
                    CreditCard(id = "2", userId = "2", name = "Inter", closingDay = 10, dueDay = 17)
                )
            ),
            onNavigateBack = {},
            onUnarchiveCreditCardClick = {}
        )
    }
}
