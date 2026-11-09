package com.moduxi.monexi.presentation.settings.credit.bills

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.creditcard.BillStatus
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.creditcard.CreditCardBill
import com.moduxi.monexi.ui.theme.MonexiTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreditCardBillsScreen(
    onNavigateBack: () -> Unit,
    onNavigateEdit: (String) -> Unit,
    onNavigateToBillDetail: (String, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreditCardBillsViewModel = viewModel(factory = CreditCardBillsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    CreditCardBillsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateEdit = onNavigateEdit,
        onNavigateToBillDetail = onNavigateToBillDetail,
        modifier = modifier
    )
}

@Composable
fun CreditCardBillsContent(
    uiState: CreditCardBillsUiState,
    onNavigateBack: () -> Unit,
    onNavigateEdit: (String) -> Unit,
    onNavigateToBillDetail: (String, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text(
                text = uiState.creditCard?.name ?: "Faturas do Cartão",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    uiState.creditCard?.id?.let { cardId ->
                        onNavigateEdit(cardId)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Cartão"
                )
            }
        }

        if (uiState.bills.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma fatura encontrada para este cartão.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(uiState.bills, key = { it.id }) { bill ->
                    CreditCardBillItem(
                        bill = bill,
                        onClick = {
                            uiState.creditCard?.id?.let { cardId ->
                                onNavigateToBillDetail(cardId, bill.month, bill.year)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditCardBillItem(
    bill: CreditCardBill,
    onClick: () -> Unit
) {
    val formattedAmount = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(bill.totalAmount)

    val (statusLabel, statusColor) = when (bill.status) {
        BillStatus.PAID -> "Paga" to Color(0xFF1B8A5A)
        BillStatus.PARTIALLY_PAID -> "Parcial" to Color(0xFF0288D1)
        BillStatus.OVERDUE -> "Atrasada" to MaterialTheme.colorScheme.error
        BillStatus.CLOSED -> "Fechada" to Color(0xFFF57C00)
        BillStatus.OPEN -> "Aberta" to Color(0xFF1565C0)
        BillStatus.FUTURE -> "Futura" to MaterialTheme.colorScheme.onSurfaceVariant
    }

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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Fatura ${bill.month}/${bill.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Text(
                    text = "${bill.transactions.size} transação(ões)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedAmount,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (bill.status == BillStatus.PAID) Color(0xFF1B8A5A) else MaterialTheme.colorScheme.primary
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreditCardBillsScreenPreview() {
    MonexiTheme {
        CreditCardBillsContent(
            uiState = CreditCardBillsUiState(
                creditCard = CreditCard(id = "1", userId = "1", name = "Nubank"),
                bills = listOf(
                    CreditCardBill(
                        id = "1_2026_11",
                        creditCardId = "1",
                        month = 11,
                        year = 2026,
                        closingDateMillis = System.currentTimeMillis(),
                        dueDateMillis = System.currentTimeMillis(),
                        totalAmount = 1450.50,
                        transactions = emptyList(),
                        isPaid = true,
                        status = BillStatus.PAID
                    )
                )
            ),
            onNavigateBack = {},
            onNavigateEdit = {},
            onNavigateToBillDetail = { _, _, _ -> }
        )
    }
}
