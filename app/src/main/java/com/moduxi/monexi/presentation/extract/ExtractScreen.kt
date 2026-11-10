package com.moduxi.monexi.presentation.extract

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.ui.theme.MonexiTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExtractScreen(
    modifier: Modifier = Modifier,
    viewModel: ExtractViewModel = viewModel(factory = ExtractViewModel.Factory),
    onNavigateToTransaction: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ExtractContent(
        uiState = uiState,
        onSearchChange = viewModel::onSearchChange,
        onPreviousMonthClick = viewModel::previousMonth,
        onNextMonthClick = viewModel::nextMonth,
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
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Entradas", "Contas", "Cartões", "Faturas Pagas")

    val filteredByType = when (selectedFilter) {
        "Entradas" -> uiState.transactions.filter { it.type == TransactionType.INCOME }
        "Contas" -> uiState.transactions.filter { it.accountId != null && it.creditCardBillId == null }
        "Cartões" -> uiState.transactions.filter { it.creditCardId != null && it.creditCardBillId == null }
        "Faturas Pagas" -> uiState.transactions.filter { it.creditCardBillId != null }
        else -> uiState.transactions
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    }

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
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Extrato",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonthClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Mês anterior"
                    )
                }

                Text(
                    text = uiState.selectedMonthName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onNextMonthClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Próximo mês"
                    )
                }
            }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum lançamento encontrado para este período.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { transaction ->
                val originAccount = uiState.accounts.firstOrNull { it.id == transaction.accountId }
                val originCard = uiState.creditCards.firstOrNull { it.id == transaction.creditCardId }

                TransactionItem(
                    transaction = transaction,
                    originAccount = originAccount,
                    originCard = originCard,
                    onClick = { onTransactionClick(transaction.id) }
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    originAccount: Account?,
    originCard: CreditCard?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isScheduled = transaction.date > System.currentTimeMillis()
    val isBillPayment = transaction.creditCardBillId != null

    // Paleta centralizada
    val incomeGreen = Color(0xFF1B8A5A)     // entrada
    val billPaymentRed = Color(0xFFC62828)  // pagamento de fatura
    val cardEntryBlue = Color(0xFF1565C0)   // lançamento na fatura

    Card(
        modifier = modifier
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Título: weight garante que os chips tenham SEMPRE a largura natural deles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isBillPayment) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFDECEA)) // fundo vermelho claro
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Pagamento Fatura",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = billPaymentRed,      // vermelho
                                maxLines = 1
                            )
                        }
                    }

                    if (isScheduled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Agendado",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Subtítulo: categoria com weight para nunca espremer origem e data
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = transaction.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val originText = when {
                        isBillPayment && originAccount != null -> "• 🏦 ${originAccount.name}"
                        originCard != null -> "• 💳 ${originCard.name}"
                        originAccount != null -> "• 🏦 ${originAccount.name}"
                        else -> ""
                    }

                    if (originText.isNotEmpty()) {
                        Text(
                            text = originText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = "• ${transaction.date.toFormattedDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            val amountColor = when {
                transaction.type == TransactionType.INCOME -> incomeGreen
                isBillPayment -> billPaymentRed   // vermelho: pagamento de fatura
                else -> cardEntryBlue             // azul: lançamento na fatura
            }

            Text(
                text = transaction.signedAmount(),
                color = amountColor,
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

private fun Long.toFormattedDate(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
    return formatter.format(Date(this))
}

@Preview(showBackground = true)
@Composable
fun ExtractPreview() {
    MonexiTheme {
        ExtractContent(
            uiState = ExtractUiState(
                transactions = listOf(
                    Transaction(
                        id = "1",
                        title = "Salário",
                        amount = 5000.0,
                        type = TransactionType.INCOME,
                        category = Category(id = "1", name = "Trabalho", type = TransactionType.INCOME),
                        paymentMethod = PaymentMethod(id = "1", name = "Pix"),
                        date = System.currentTimeMillis()
                    ),
                    Transaction(
                        id = "2",
                        title = "Supermercado",
                        amount = 350.0,
                        type = TransactionType.EXPENSE,
                        category = Category(id = "2", name = "Alimentação", type = TransactionType.EXPENSE),
                        paymentMethod = PaymentMethod(id = "2", name = "Cartão de Crédito"),
                        creditCardId = "c1",
                        date = System.currentTimeMillis()
                    ),
                    Transaction(
                        id = "3",
                        title = "Pagamento Fatura - Nubank (11/2026)",
                        amount = 350.0,
                        type = TransactionType.EXPENSE,
                        category = Category(id = "3", name = "Fatura de Cartão", type = TransactionType.EXPENSE),
                        paymentMethod = PaymentMethod(id = "1", name = "Pix"),
                        accountId = "a1",
                        creditCardBillId = "c1_2026_11",
                        date = System.currentTimeMillis()
                    )
                ),
                accounts = listOf(
                    Account(id = "a1", name = "Conta Nubank")
                ),
                creditCards = listOf(
                    CreditCard(id = "c1", name = "Nubank Gold")
                )
            ),
            onSearchChange = {},
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            onTransactionClick = {}
        )
    }
}
