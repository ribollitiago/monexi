package com.moduxi.monexi.presentation.settings.accounts

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.Account
import com.moduxi.monexi.ui.theme.MonexiTheme
import com.moduxi.monexi.ui.theme.toCategoryIcon
import com.moduxi.monexi.ui.theme.toComposeColor
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BankAccountScreen(
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    onNavigateEdit: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BankAccountViewModel = viewModel(factory = BankAccountViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    BankAccountContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateArchived = onNavigateArchived,
        onNavigateEdit = { onNavigateEdit(null) },
        onAccountClick = { account -> onNavigateEdit(account.id) },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun BankAccountContent(
    uiState: BankAccountUiState,
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    onNavigateEdit: () -> Unit,
    onAccountClick: (Account) -> Unit,
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
                    text = "Contas",
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
                items(uiState.accounts) { account ->
                    BankAccountItem(
                        bankAccount = account,
                        onClick = { onAccountClick(account) }
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
private fun BankAccountItem(
    bankAccount: Account,
    onClick: () -> Unit
) {
    val formattedBalance = remember(bankAccount.initialBalance) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        formatter.format(bankAccount.initialBalance)
    }

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
            val cardColor = bankAccount.colorHex.toComposeColor()

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(cardColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = bankAccount.iconName.toCategoryIcon(),
                    contentDescription = bankAccount.name,
                    tint = cardColor
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = bankAccount.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Saldo $formattedBalance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BankAccountScreenPreview() {
    MonexiTheme {
        BankAccountContent(
            uiState = BankAccountUiState(
                accounts = listOf(
                    Account(id = "1", name = "Nubank", initialBalance = 100.0)
                )
            ),
            onNavigateBack = {},
            onNavigateArchived = {},
            onNavigateEdit = {},
            onAccountClick = {}
        )
    }
}
