package com.moduxi.monexi.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionScreen (
    modifier: Modifier = Modifier,
    viewModel: TransactionViewModel = viewModel()
) {
    // 1. Comentei a linha que está dando erro
    //val uiState by viewModel.uiState.collectAsState()

    // 2. Criei um estado "falso" temporário só para a tela compilar e o Preview funcionar
    val uiState = TransactionUiState()

    TransactionContent(
        uiState = uiState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionContent (
    uiState: TransactionUiState,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf(0) }
    var description by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()
    var valueDate by remember { mutableStateOf("")}

    var selectedPayment by remember { mutableStateOf<PaymentMethod?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    //Temporário
    val categories = listOf(
        Category(id = 1, name = "Alimentacao", isDefault = true),
        Category(id = 2, name = "Transporte", isDefault = true),
        Category(id = 3, name = "Casa", isDefault = true),
        Category(id = 4, name = "Trabalho", isDefault = true)
    )

    //Temporário
    val paymentMethods = listOf(
        PaymentMethod(id = 1, name = "Dinheiro", isDefault = true),
        PaymentMethod(id = 2, name = "Pix", isDefault = true),
        PaymentMethod(id = 3, name = "Cartao de debito", isDefault = true),
        PaymentMethod(id = 4, name = "Cartao de credito", isDefault = true)
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
                Text(
                    text = "Nova Transação",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                val buttons = listOf("Receita", "A Receber", "Custo", "Despesa")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttons.forEachIndexed { index, label ->
                        val isSelected = selectedIndex == index

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    selectedIndex = index
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
                    value = description,
                    onValueChange = { newDescription ->
                        description = newDescription
                    },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Data") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )
            }
            item {
                OutlinedTextField(
                    value = valueDate,
                    onValueChange = { newValue -> valueDate = newValue },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                DropdownField(
                    label = "Forma de Pagamento",
                    options = paymentMethods,
                    selectedOption = selectedPayment,
                    onOptionSelected = { selectedPayment = it },
                    optionLabel = { it.name }
                )
            }

            item {
                DropdownField(
                    label = "Categoria",
                    options = categories,
                    selectedOption = selectedCategory,
                    onOptionSelected = { selectedCategory = it },
                    optionLabel = { it.name }
                )
            }
        }
        Button(
            onClick = {

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = "Salvar Alterações")
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownField(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false}
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(optionLabel(selectionOption))},
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionScreenPreview() {
    MaterialTheme {
        TransactionContent (
            uiState = TransactionUiState()
        )
    }
}