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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

    var showPayDialog by remember { mutableStateOf(false) }

    val selectedAccount = remember(uiState.accounts, creditCard) {
        uiState.accounts.firstOrNull { it.id == creditCard?.linkedAccountId }
            ?: uiState.accounts.firstOrNull()
    }

    val selectedPaymentMethod = remember(uiState.paymentMethods) {
        uiState.paymentMethods.firstOrNull { it.name.contains("Pix", ignoreCase = true) || it.name.contains("Débito", ignoreCase = true) }
            ?: uiState.paymentMethods.firstOrNull()
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedAmount,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Button(
                            onClick = { showPayDialog = true }
                        ) {
                            Text("Pagar Fatura")
                        }
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

    if (showPayDialog && bill != null) {
        PayBillDialog(
            billAmount = formattedAmount,
            accounts = uiState.accounts,
            paymentMethods = uiState.paymentMethods,
            initialSelectedAccount = selectedAccount,
            initialSelectedPaymentMethod = selectedPaymentMethod,
            onDismiss = { showPayDialog = false },
            onConfirm = { account, paymentMethod ->
                onPayBill(bill, account.id, paymentMethod)
                showPayDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayBillDialog(
    billAmount: String,
    accounts: List<Account>,
    paymentMethods: List<PaymentMethod>,
    initialSelectedAccount: Account?,
    initialSelectedPaymentMethod: PaymentMethod?,
    onDismiss: () -> Unit,
    onConfirm: (Account, PaymentMethod) -> Unit
) {
    val validPaymentMethods = remember(paymentMethods) {
        paymentMethods.filter { !it.name.contains("Crédito", ignoreCase = true) }
    }

    var selectedAccount by remember(accounts) {
        mutableStateOf(initialSelectedAccount ?: accounts.firstOrNull())
    }
    var selectedPaymentMethod by remember(validPaymentMethods) {
        mutableStateOf(
            validPaymentMethods.firstOrNull { it.name.contains("Pix", ignoreCase = true) || it.name.contains("Débito", ignoreCase = true) }
                ?: validPaymentMethods.firstOrNull()
        )
    }

    var accountExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pagar Fatura", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Valor Total: $billAmount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (accounts.isNotEmpty() && selectedAccount != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Conta de Débito:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        ExposedDropdownMenuBox(
                            expanded = accountExpanded,
                            onExpandedChange = { accountExpanded = !accountExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedAccount!!.name,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = accountExpanded,
                                onDismissRequest = { accountExpanded = false }
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.name) },
                                        onClick = {
                                            selectedAccount = acc
                                            accountExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text("Nenhuma conta cadastrada para débito.", color = MaterialTheme.colorScheme.error)
                }

                if (validPaymentMethods.isNotEmpty() && selectedPaymentMethod != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Forma de Pagamento:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        ExposedDropdownMenuBox(
                            expanded = methodExpanded,
                            onExpandedChange = { methodExpanded = !methodExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedPaymentMethod!!.name,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = methodExpanded,
                                onDismissRequest = { methodExpanded = false }
                            ) {
                                validPaymentMethods.forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method.name) },
                                        onClick = {
                                            selectedPaymentMethod = method
                                            methodExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedAccount != null && selectedPaymentMethod != null,
                onClick = {
                    val acc = selectedAccount
                    val method = selectedPaymentMethod
                    if (acc != null && method != null) {
                        onConfirm(acc, method)
                    }
                }
            ) {
                Text("Confirmar Pagamento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
