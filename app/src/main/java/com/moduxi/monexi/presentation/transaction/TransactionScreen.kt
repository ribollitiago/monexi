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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.ui.theme.CurrencyVisualTransformation
import com.moduxi.monexi.ui.theme.toCategoryIcon
import com.moduxi.monexi.ui.theme.toComposeColor
import java.text.SimpleDateFormat
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
        onDescriptionChange = viewModel::onDescriptionChange,
        onAmountChange = viewModel::onAmountChange,
        onDateChange = viewModel::onDateChange,
        onCategoryChange = viewModel::onCategoryChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
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
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.dateMillis
    )

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
                Row (
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
                            text = if (uiState.editingTransaction == null) "Nova Transação" else "Editar Transação",
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
                val transactionTypes = listOf(
                    TransactionType.EXPENSE to "Despesa",
                    TransactionType.INCOME to "Receita"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    transactionTypes.forEach { (type, label) ->
                        val isSelected = uiState.type == type

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    onTypeChange(type)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
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
                DropdownField(
                    label = "Forma de Pagamento",
                    options = uiState.paymentMethods,
                    selectedOption = uiState.selectedPaymentMethod,
                    onOptionSelected = onPaymentMethodChange,
                    optionLabel = { it.name }
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
            Text(text = "Salvar")
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
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    optionIcon: ((T) -> ImageVector?)? = null,
    optionColor: ((T) -> Color?)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption?.let { optionLabel(it) } ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            leadingIcon = if (selectedOption != null && optionColor != null) {
                {
                    val icon = optionIcon?.invoke(selectedOption)
                    val tint = optionColor?.invoke(selectedOption) ?: MaterialTheme.colorScheme.primary
                    if (icon != null){
                        Icon(imageVector = icon, contentDescription = null, tint = tint)
                    }
                }
            } else null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false}
        ) {
            options.forEach { selectionOption ->
                val icon = optionIcon?.invoke(selectionOption)
                val tint = optionColor?.invoke(selectionOption) ?: MaterialTheme.colorScheme.onSurface

                DropdownMenuItem(
                    text = { Text(optionLabel(selectionOption))},
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    leadingIcon = if (icon != null) {
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint
                            )
                        }
                    } else null
                )
            }
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
            onDescriptionChange = {},
            onAmountChange = {},
            onDateChange = {},
            onCategoryChange = {},
            onPaymentMethodChange = {},
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