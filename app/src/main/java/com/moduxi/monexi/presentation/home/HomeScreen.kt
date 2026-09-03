package com.moduxi.monexi.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.ui.theme.MonexiTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToTransaction: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = uiState,
        onAddTransactionClick = {
            onNavigateToTransaction()
        },
        modifier = modifier
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Monexi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SummaryCard(
                title = "Saldo atual",
                amount = uiState.balance,
                amountColor = if (uiState.balance >= 0) Color(0xFF1B8A5A) else Color(0xFFC62828)
            )
        }

        item {
            Button(
                onClick = onAddTransactionClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Adicionar despesa teste")
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Receitas",
                    amount = uiState.totalIncome,
                    amountColor = Color(0xFF1B8A5A),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Despesas",
                    amount = uiState.totalExpense,
                    amountColor = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ultimas transacoes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(uiState.transactions) { transaction ->
            TransactionItem(transaction = transaction)
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    amountColor: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount.toCurrency(),
                color = amountColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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
private fun Double.toCurrency(): String {
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    }
    return formatter.format(this)
}

@Composable
private fun Transaction.signedAmount(): String {
    val prefix = if (type == TransactionType.INCOME) "+" else "-"
    return "$prefix ${amount.toCurrency()}"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MonexiTheme {
        HomeContent(
            uiState = HomeUiState(
                balance = 2800.0,
                totalIncome = 3200.0,
                totalExpense = 400.0,
                transactions = listOf(
                    Transaction(
                        1,
                        "Salario",
                        3200.0,
                        TransactionType.INCOME,
                        Category(
                            id = 1,
                            name = "Alimentação"
                        ),
                        PaymentMethod(
                            id = 1,
                            name = "Pix"
                        ),
                        System.currentTimeMillis()
                    ),
                    Transaction(
                        2,
                        "Mercado",
                        280.0,
                        TransactionType.EXPENSE,
                        Category(
                            id = 1,
                            name = "Alimentação"
                        ),
                        PaymentMethod(
                            id = 1,
                            name = "Pix"
                        ),
                        System.currentTimeMillis())
                )
            ),
            onAddTransactionClick = {}
        )
    }
}
