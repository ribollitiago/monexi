package com.moduxi.monexi.presentation.extract

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.ui.theme.MonexiTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ExtractScreen(
    modifier: Modifier = Modifier,
    viewModel: ExtractViewModel = viewModel(factory = ExtractViewModel .Factory),
    onNavigateToTransaction: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ExtractContent(
        uiState = uiState,
        onSearchChange = { query ->
            viewModel.onSearchChange(query)
        },
        onTransactionClick = { id ->
            onNavigateToTransaction(id)
        },
        modifier = modifier
    )
}

@Composable
fun ExtractContent(
    uiState: ExtractUiState,
    onSearchChange: (String) -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Entrada", "Saida")

    val filteredByType = when (selectedFilter) {
        "Entrada" -> uiState.transactions.filter { it.type == TransactionType.INCOME }
        "Saida" -> uiState.transactions.filter { it.type == TransactionType.EXPENSE }
        else -> uiState.transactions
    }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))

    val filteredTransactions = if (uiState.search.isBlank()) {
        filteredByType
    } else {
        filteredByType.filter { transaction ->
            val formattedAmount = currencyFormatter.format(transaction.amount)

            transaction.title.contains(uiState.search, ignoreCase = true) ||
            transaction.category.name.contains(uiState.search, ignoreCase = true) ||
            formattedAmount.contains(uiState.search, ignoreCase = true) ||
            transaction.amount.toString().contains(uiState.search)
        }
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Lançamentos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = uiState.search,
                onValueChange = onSearchChange,
                label = { Text("Buscar...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row (
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter},
                        label = { Text(filter) }
                    )
                }
            }
        }

        items(filteredTransactions) { transaction ->
            TransactionItem(
                transaction = transaction,
                onClick = { onTransactionClick(transaction.id) }
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = transaction.category.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = transaction.signedAmount(),
                color = if (transaction.type == TransactionType.INCOME) Color(0xFF1B8A5A) else Color(0xFFC62828),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Transaction.signedAmount(): String {
    val prefix = if (type == TransactionType.INCOME) "+" else "-"
    return "$prefix ${amount.toCurrency()}"
}

@Composable
private fun Double.toCurrency(): String {
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    }
    return formatter.format(this)
}

@Preview(showBackground = true)
@Composable
fun ExtractPreview() {
    MonexiTheme() {
        ExtractContent(
            uiState = ExtractUiState(
                "",
                listOf(
                    Transaction(
                        id = "1",
                        userId = "1",
                        title = "Salario",
                        amount = 3200.0,
                        type = TransactionType.INCOME,
                        category = Category(
                            id = "1",
                            userId = "1",
                            name = "Alimentação",
                            type = TransactionType.INCOME
                        ),
                        paymentMethod = PaymentMethod(
                            id = "1",
                            userId = "1",
                            name = "Pix"
                        ),
                        date = System.currentTimeMillis()
                    ),
                )
            ),
            onSearchChange = {},
            onTransactionClick = {}
        )
    }
}