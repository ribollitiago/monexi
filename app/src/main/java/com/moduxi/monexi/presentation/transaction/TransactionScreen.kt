package com.moduxi.monexi.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.account.Account
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.ui.components.DropdownField
import com.moduxi.monexi.ui.theme.CurrencyVisualTransformation
import com.moduxi.monexi.ui.theme.toCategoryIcon
import com.moduxi.monexi.ui.theme.toComposeColor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun TransactionScreen(
    onTransactionSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    TransactionContent(
        uiState = uiState,
        onTypeChange = viewModel::onTypeChange,
        onIsTransferChange = viewModel::onIsTransferChange,
        onIsRecurringChange = viewModel::onIsRecurringChange,
        onRecurringDayOfMonthChange = viewModel::onRecurringDayOfMonthChange,
        onIsRecurringInfiniteChange = viewModel::onIsRecurringInfiniteChange,
        onRecurringEndDateChange = viewModel::onRecurringEndDateChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onAmountChange = viewModel::onAmountChange,
        onDateChange = viewModel::onDateChange,
        onInstallmentsChange = viewModel::onInstallmentsChange,
        onCategoryChange = viewModel::onCategoryChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
        onAccountChange = viewModel::onAccountChange,
        onDestinationAccountChange = viewModel::onDestinationAccountChange,
        onCreditCardChange = viewModel::onCreditCardChange,
        onDeleteClick = {
            uiState.editingTransaction?.let { transaction ->
                viewModel.deleteTransaction(transaction)
                onTransactionSaved()
            }
        },
        onSaveClick = {
            viewModel.saveTransaction(
                onSaved = onTransactionSaved
            )
        },
        onBackClick = onTransactionSaved,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionContent(
    uiState: TransactionUiState,
    onTypeChange: (TransactionType) -> Unit,
    onIsTransferChange: (Boolean) -> Unit,
    onIsRecurringChange: (Boolean) -> Unit,
    onRecurringDayOfMonthChange: (Int) -> Unit,
    onIsRecurringInfiniteChange: (Boolean) -> Unit,
    onRecurringEndDateChange: (Long?) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onInstallmentsChange: (Int) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onAccountChange: (Account) -> Unit,
    onDestinationAccountChange: (Account) -> Unit,
    onCreditCardChange: (CreditCard) -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.dateMillis
    )

    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.recurringEndDateMillis ?: (System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)
    )

    val availablePaymentMethods = remember(uiState.paymentMethods, uiState.type) {
        if (uiState.type == TransactionType.INCOME) {
            uiState.paymentMethods.filter { !it.name.contains("Crédito", ignoreCase = true) }
        } else {
            uiState.paymentMethods
        }
    }

    val isCreditCardSelected = !uiState.isTransfer &&
            uiState.type == TransactionType.EXPENSE &&
            uiState.selectedPaymentMethod?.name?.contains("Crédito", ignoreCase = true) == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.editingTransaction != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.isTransfer) {
                                "Transferência entre Contas"
                            } else if (uiState.editingTransaction == null) {
                                "Nova Transação"
                            } else {
                                "Editar Transação"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uiState.editingTransaction != null) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Transação",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modeExpenseSelected = !uiState.isTransfer && uiState.type == TransactionType.EXPENSE
                    val modeIncomeSelected = !uiState.isTransfer && uiState.type == TransactionType.INCOME
                    val modeTransferSelected = uiState.isTransfer

                    // Despesa
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (modeExpenseSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onTypeChange(TransactionType.EXPENSE) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Despesa",
                            color = if (modeExpenseSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Receita
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (modeIncomeSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onTypeChange(TransactionType.INCOME) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Receita",
                            color = if (modeIncomeSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Transferência
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (modeTransferSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onIsTransferChange(true) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Transferência",
                            color = if (modeTransferSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (uiState.isTransfer) {
                // Form de Transferência entre contas
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DropdownField(
                            label = "Conta de Origem (Sairá de)",
                            options = uiState.accounts,
                            selectedOption = uiState.selectedAccount,
                            onOptionSelected = onAccountChange,
                            optionLabel = { it.name },
                            optionIcon = { it.iconName.toCategoryIcon() },
                            optionColor = { it.colorHex.toComposeColor() }
                        )
                        uiState.sourceAccountBalance?.let { balance ->
                            val formattedBalance = remember(balance) {
                                NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(balance)
                            }
                            Text(
                                text = "Saldo disponível: $formattedBalance",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (balance >= 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                item {
                    val availableDestinations = remember(uiState.accounts, uiState.selectedAccount) {
                        uiState.accounts.filter { it.id != uiState.selectedAccount?.id }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DropdownField(
                            label = "Conta de Destino (Irá para)",
                            options = availableDestinations,
                            selectedOption = uiState.selectedDestinationAccount,
                            onOptionSelected = onDestinationAccountChange,
                            optionLabel = { it.name },
                            optionIcon = { it.iconName.toCategoryIcon() },
                            optionColor = { it.colorHex.toComposeColor() }
                        )
                        uiState.destinationAccountBalance?.let { balance ->
                            val formattedBalance = remember(balance) {
                                NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(balance)
                            }
                            Text(
                                text = "Saldo atual: $formattedBalance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = uiState.amountDigits,
                        onValueChange = onAmountChange,
                        label = { Text("Valor a Transferir") },
                        placeholder = { Text("0,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = CurrencyVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.dateMillis.toBrazilianDateFormat(),
                        onValueChange = {},
                        label = { Text("Data da Transferência") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusEvent {
                                if (it.isFocused) {
                                    showDatePicker = true
                                    focusManager.clearFocus(force = true)
                                }
                            }
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Descrição / Observação (Opcional)") },
                        placeholder = { Text("ex: Reserva mensal") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Form de Despesa / Receita comum
                item {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.amountDigits,
                        onValueChange = onAmountChange,
                        label = { Text("Valor") },
                        placeholder = { Text("0,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = CurrencyVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.dateMillis.toBrazilianDateFormat(),
                        onValueChange = {},
                        label = { Text("Data") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusEvent {
                                if (it.isFocused) {
                                    showDatePicker = true
                                    focusManager.clearFocus(force = true)
                                }
                            }
                    )
                }

                item {
                    DropdownField(
                        label = "Categoria",
                        options = uiState.categories,
                        selectedOption = uiState.selectedCategory,
                        onOptionSelected = onCategoryChange,
                        optionLabel = { it.name },
                        optionIcon = { it.iconName.toCategoryIcon() },
                        optionColor = { it.colorHex.toComposeColor() }
                    )
                }

                item {
                    DropdownField(
                        label = "Forma de Pagamento",
                        options = availablePaymentMethods,
                        selectedOption = uiState.selectedPaymentMethod,
                        onOptionSelected = onPaymentMethodChange,
                        optionLabel = { it.name }
                    )
                }

                if (isCreditCardSelected && uiState.creditCards.isNotEmpty()) {
                    item {
                        DropdownField(
                            label = "Cartão de Crédito",
                            options = uiState.creditCards,
                            selectedOption = uiState.selectedCreditCard,
                            onOptionSelected = onCreditCardChange,
                            optionLabel = { it.name },
                            optionIcon = { it.iconName.toCategoryIcon() },
                            optionColor = { it.colorHex.toComposeColor() }
                        )
                    }

                    item {
                        DropdownField(
                            label = "Parcelamento",
                            options = (1..12).toList(),
                            selectedOption = uiState.totalInstallments,
                            onOptionSelected = onInstallmentsChange,
                            optionLabel = { count -> if (count == 1) "À vista / 1x" else "${count}x" }
                        )
                    }

                    item {
                        val totalAmount = (uiState.amountDigits.toLongOrNull() ?: 0L) / 100.0
                        val count = uiState.totalInstallments
                        val installmentVal = if (count > 0) totalAmount / count else totalAmount
                        val formattedInstallment = remember(installmentVal) {
                            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                            formatter.format(installmentVal)
                        }
                        val closingDay = uiState.selectedCreditCard?.closingDay ?: 5
                        val invoiceMonth = remember(uiState.dateMillis, closingDay) {
                            calculateInvoiceMonthName(uiState.dateMillis, closingDay)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Será lançado como",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$count parcela${if (count > 1) "s" else ""} de $formattedInstallment",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "na fatura de $invoiceMonth",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else if (!isCreditCardSelected && uiState.accounts.isNotEmpty()) {
                    item {
                        DropdownField(
                            label = "Conta / Carteira",
                            options = uiState.accounts,
                            selectedOption = uiState.selectedAccount,
                            onOptionSelected = onAccountChange,
                            optionLabel = { it.name },
                            optionIcon = { it.iconName.toCategoryIcon() },
                            optionColor = { it.colorHex.toComposeColor() }
                        )
                    }
                }
            }

            // Opções de Recorrência
            if (uiState.editingTransaction == null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Lançamento Recorrente", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(text = "Repetir mensalmente este lançamento", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.isRecurring,
                            onCheckedChange = onIsRecurringChange
                        )
                    }
                }

                if (uiState.isRecurring) {
                    item {
                        DropdownField(
                            label = "Dia do Vencimento/Repetição",
                            options = (1..31).toList(),
                            selectedOption = uiState.recurringDayOfMonth,
                            onOptionSelected = onRecurringDayOfMonthChange,
                            optionLabel = { "Todo dia $it do mês" }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (uiState.isRecurringInfinite) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onIsRecurringInfiniteChange(true) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Infinito",
                                    color = if (uiState.isRecurringInfinite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (!uiState.isRecurringInfinite) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onIsRecurringInfiniteChange(false) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Até data final",
                                    color = if (!uiState.isRecurringInfinite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (!uiState.isRecurringInfinite) {
                        item {
                            OutlinedTextField(
                                value = (uiState.recurringEndDateMillis ?: (System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)).toBrazilianDateFormat(),
                                onValueChange = {},
                                label = { Text("Data Limite de Término") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusEvent {
                                        if (it.isFocused) {
                                            showEndDatePicker = true
                                            focusManager.clearFocus(force = true)
                                        }
                                    }
                            )
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = if (uiState.isTransfer) "Confirmar Transferência" else "Salvar")
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateChange(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(text = "Escolher data")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            onRecurringEndDateChange(millis)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text(text = "Escolher data")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionScreenPreview() {
    MaterialTheme {
        TransactionContent(
            uiState = TransactionUiState(),
            onTypeChange = {},
            onIsTransferChange = {},
            onIsRecurringChange = {},
            onRecurringDayOfMonthChange = {},
            onIsRecurringInfiniteChange = {},
            onRecurringEndDateChange = {},
            onDescriptionChange = {},
            onAmountChange = {},
            onDateChange = {},
            onInstallmentsChange = {},
            onCategoryChange = {},
            onPaymentMethodChange = {},
            onAccountChange = {},
            onDestinationAccountChange = {},
            onCreditCardChange = {},
            onDeleteClick = {},
            onSaveClick = {},
            onBackClick = {}
        )
    }
}

private fun Long.toBrazilianDateFormat(
    pattern: String = "dd/MM/yyyy"
): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(
        pattern, Locale.forLanguageTag("pt-BR")
    ).apply {
        timeZone = TimeZone.getTimeZone("GMT")
    }
    return formatter.format(date)
}

private fun calculateInvoiceMonthName(transactionDateMillis: Long, closingDay: Int): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = transactionDateMillis
    }
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    if (day > closingDay) {
        calendar.add(Calendar.MONTH, 1)
    }
    val monthFormat = SimpleDateFormat("MMMM", Locale.forLanguageTag("pt-BR"))
    return monthFormat.format(calendar.time)
}
