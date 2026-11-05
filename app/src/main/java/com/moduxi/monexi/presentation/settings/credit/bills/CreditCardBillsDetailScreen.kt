package com.moduxi.monexi.presentation.settings.credit.bills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.account.AccountType
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.creditcard.CreditCardBill
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.ui.theme.MonexiTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreditCardBillDetailScreen(
    month: Int,
    year: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreditCardBillsViewModel = viewModel(factory = CreditCardBillsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    CreditCardBillDetailContent(
        month = month,
        year = year,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onPayBill = { bill, accountId, paymentMethod ->
            viewModel.payBill(bill, accountId, paymentMethod, onNavigateBack)
        },
        modifier = modifier
    )
}

@Composable
fun CreditCardBillDetailContent(
    month: Int,
    year: Int,
    uiState: CreditCardBillsUiState,
    onNavigateBack: () -> Unit,
    onPayBill: (CreditCardBill, String, PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    val bill = uiState.bills.firstOrNull { it.month == month && it.year == year }
    val creditCard = uiState.creditCard

    var selectedAccount by remember(uiState.accounts, creditCard) {
        mutableStateOf(
            uiState.accounts.firstOrNull { it.id == creditCard?.linkedAccountId }
                ?: uiState.accounts.firstOrNull()
        )
    }

    var selectedPaymentMethod by remember(uiState.paymentMethods) {
        mutableStateOf(
            uiState.paymentMethods.firstOrNull { it.name.contains("Pix", ignoreCase = true) || it.name.contains("Débito", ignoreCase = true) }
                ?: uiState.paymentMethods.firstOrNull()
        )
    }

    val formattedAmount = remember(bill) {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(bill?.totalAmount ?: 0.0)
    }

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
                text = "Fatura de $month/$year",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Box(modifier = Modifier.padding(24.dp))
        }

        if (bill == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Fatura não encontrada.")
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Total da Fatura", style = MaterialTheme.typography.bodyMedium)
                    Text(text = formattedAmount, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (uiState.accounts.isNotEmpty() && selectedAccount != null && selectedPaymentMethod != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Pagar com a Conta:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    AccountDropdown(
                        accounts = uiState.accounts,
                        selectedAccount = selectedAccount!!,
                        onAccountSelected = { selectedAccount = it }
                    )

                    Button(
                        onClick = {
                            onPayBill(bill, selectedAccount!!.id, selectedPaymentMethod!!)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pagar Fatura")
                    }
                }
            }

            Text(text = "Lançamentos da Fatura", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bill.transactions, key = { it.id }) { tx ->
                    val txAmount = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(tx.amount)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = tx.title, fontWeight = FontWeight.SemiBold)
                                Text(text = tx.category.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(text = txAmount, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdown(
    accounts: List<Account>,
    selectedAccount: Account,
    onAccountSelected: (Account) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedAccount.name,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreditCardBillDetailPreview() {
    MonexiTheme {
        CreditCardBillDetailContent(
            month = 11,
            year = 2026,
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
                        transactions = listOf(
                            Transaction(
                                id = "t1",
                                title = "Supermercado",
                                amount = 350.50,
                                type = TransactionType.EXPENSE,
                                category = Category(id = "c1", name = "Alimentação", type = TransactionType.EXPENSE),
                                paymentMethod = PaymentMethod(id = "p1", name = "Cartão de Crédito"),
                                date = System.currentTimeMillis()
                            ),
                            Transaction(
                                id = "t2",
                                title = "Posto de Gasolina",
                                amount = 1100.00,
                                type = TransactionType.EXPENSE,
                                category = Category(id = "c2", name = "Transporte", type = TransactionType.EXPENSE),
                                paymentMethod = PaymentMethod(id = "p1", name = "Cartão de Crédito"),
                                date = System.currentTimeMillis()
                            )
                        )
                    )
                ),
                accounts = listOf(
                    Account(id = "a1", name = "Nubank", initialBalance = 2500.0, type = AccountType.CHECKING)
                ),
                paymentMethods = listOf(
                    PaymentMethod(id = "p2", name = "Pix")
                )
            ),
            onNavigateBack = {},
            onPayBill = { _, _, _ -> }
        )
    }
}
