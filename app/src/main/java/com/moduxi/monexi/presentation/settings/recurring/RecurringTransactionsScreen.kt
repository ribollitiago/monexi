package com.moduxi.monexi.presentation.settings.recurring

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.ui.theme.MonexiTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecurringTransactionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecurringTransactionsViewModel = viewModel(factory = RecurringTransactionsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    RecurringTransactionsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onDeleteRecurring = viewModel::deleteRecurringTransaction,
        modifier = modifier
    )
}

@Composable
fun RecurringTransactionsContent(
    uiState: RecurringTransactionsUiState,
    onNavigateBack: () -> Unit,
    onDeleteRecurring: (RecurringTransaction, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedForDelete by remember { mutableStateOf<RecurringTransaction?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lançamentos Recorrentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.size(48.dp))
        }

        if (uiState.recurringTransactions.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum lançamento recorrente cadastrado.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.recurringTransactions, key = { it.id }) { recurring ->
                    val formattedAmount = remember(recurring.amount) {
                        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(recurring.amount)
                    }
                    val isIncome = recurring.type == TransactionType.INCOME

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = recurring.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Todo dia ${recurring.dayOfMonth} do mês • ${if (recurring.endDate == null) "Infinito" else "Até ${recurring.endDate.toBrazilianDateFormat()}"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formattedAmount,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }

                            IconButton(onClick = { selectedForDelete = recurring }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir Recorrência",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedForDelete?.let { recurring ->
        AlertDialog(
            onDismissRequest = { selectedForDelete = null },
            title = { Text("Cancelar Recorrência", fontWeight = FontWeight.Bold) },
            text = {
                Text("Deseja cancelar '${recurring.title}'? Escolha se deseja manter ou apagar os lançamentos passados no extrato.")
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onDeleteRecurring(recurring, false)
                            selectedForDelete = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apenas as futuras (Manter histórico)")
                    }

                    OutlinedButton(
                        onClick = {
                            onDeleteRecurring(recurring, true)
                            selectedForDelete = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Excluir tudo (Apenas histórico e futuras)")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedForDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun Long.toBrazilianDateFormat(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
    return formatter.format(date)
}

@Preview(showBackground = true)
@Composable
private fun RecurringTransactionsPreview() {
    MonexiTheme {
        RecurringTransactionsContent(
            uiState = RecurringTransactionsUiState(
                recurringTransactions = listOf(
                    RecurringTransaction(
                        id = "1",
                        userId = "1",
                        title = "Netflix",
                        amount = 55.90,
                        type = TransactionType.EXPENSE,
                        categoryId = "1",
                        accountId = "1",
                        creditCardId = null,
                        dayOfMonth = 10,
                        startDate = System.currentTimeMillis(),
                        endDate = null,
                        isActive = true
                    )
                )
            ),
            onNavigateBack = {},
            onDeleteRecurring = { _, _ -> }
        )
    }
}
