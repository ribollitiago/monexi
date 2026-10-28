package com.moduxi.monexi.presentation.settings.credit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.ui.theme.MonexiTheme
import com.moduxi.monexi.ui.theme.toCategoryIcon
import com.moduxi.monexi.ui.theme.toComposeColor

@Composable
fun CreditCardScreen(
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    onNavigateEdit: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreditCardViewModel = viewModel(factory = CreditCardViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    CreditCardContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateArchived = onNavigateArchived,
        onNavigateEdit = { onNavigateEdit(null) },
        onCreditCardClick = { creditCard -> onNavigateEdit(creditCard.id) },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun CreditCardContent(
    uiState: CreditCardUiState,
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    onNavigateEdit: () -> Unit,
    onCreditCardClick: (CreditCard) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
                Text(
                    text = "Cartões de Crédito",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNavigateArchived) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Arquivados"
                    )
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.creditCards) { creditCard ->
                    CreditCardItem(
                        creditCard = creditCard,
                        onClick = { onCreditCardClick(creditCard) }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { onNavigateEdit() },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Adicionar novo") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun CreditCardItem(
    creditCard: CreditCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreditCardScreenPreview() {
    MonexiTheme {
        CreditCardContent(
            uiState = CreditCardUiState(
                creditCards = listOf(
                    CreditCard(id = "1", userId = "1", name = "Nubank", closingDay = 5, dueDay = 12, linkedAccountId = ""),
                    CreditCard(id = "2", userId = "2", name = "Inter", closingDay = 10, dueDay = 17, linkedAccountId = "")
                )
            ),
            onNavigateBack = {},
            onNavigateArchived = {},
            onNavigateEdit = {},
            onCreditCardClick = {}
        )
    }
}
