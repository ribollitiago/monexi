package com.moduxi.monexi.presentation.settings.accounts.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.ui.theme.CurrencyVisualTransformation
import com.moduxi.monexi.ui.theme.MonexiTheme
import com.moduxi.monexi.ui.theme.toCategoryIcon
import com.moduxi.monexi.ui.theme.toComposeColor

@Composable
fun EditBankAccountScreen(
    onBankAccountSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditBankAccountViewModel = viewModel(factory = EditBankAccountViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    EditBankAccountContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onInitialBalanceChange = viewModel::onInitialBalanceChange,
        onColorChange = viewModel::onColorChange,
        onIconChange = viewModel::onIconChange,
        modifier = modifier,
        onSaveClick = {
            viewModel.saveAccount(
                onSaved = onBankAccountSaved
            )
        },
        onDeleteClick = {
            viewModel.deleteAccount(
                onDeleted = onBankAccountSaved
            )
        },
        onBackClick = onBankAccountSaved
    )
}

@Composable
fun EditBankAccountContent(
    uiState: EditBankAccountUiState,
    onNameChange: (String) -> Unit,
    onInitialBalanceChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val availableColors = listOf(
        "#1B8A5A", // Verde
        "#1565C0", // Azul
        "#C62828", // Vermelho
        "#F57C00", // Laranja
        "#6A1B9A", // Roxo
        "#00838F", // Teal
        "#D81B60", // Rosa
        "#424242"  // Grafite
    )

    val availableIcons = listOf(
        "AccountBalance" to "Banco",
        "AccountBalanceWallet" to "Carteira",
        "AttachMoney" to "Dinheiro",
        "CreditCard" to "Cartão",
        "ShoppingBag" to "Compras",
        "Work" to "Trabalho"
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }

                    Text(
                        text = if (uiState.editingBankAccount == null) "Nova Conta" else "Editar Conta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (uiState.editingBankAccount != null) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Conta",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.accountName,
                    onValueChange = onNameChange,
                    label = { Text("Nome da Conta (ex: Nubank, Itaú, Carteira)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.balanceDigits,
                    onValueChange = onInitialBalanceChange,
                    label = { Text("Saldo Inicial") },
                    placeholder = { Text("0,00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = CurrencyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Cor da Conta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(availableColors) { colorHex ->
                        val isSelected = uiState.selectedColorHex == colorHex
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colorHex.toComposeColor())
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onColorChange(colorHex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selecionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Ícone da Conta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(availableIcons) { (iconName, label) ->
                        val isSelected = uiState.selectedIconName == iconName
                        val accountColor = uiState.selectedColorHex.toComposeColor()

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) accountColor.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) accountColor else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onIconChange(iconName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconName.toCategoryIcon(),
                                contentDescription = label,
                                tint = if (isSelected) accountColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
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
}

@Preview(showBackground = true)
@Composable
fun EditBankAccountPreview() {
    MonexiTheme {
        EditBankAccountContent(
            uiState = EditBankAccountUiState(),
            onSaveClick = {},
            onDeleteClick = {},
            onBackClick = {},
            onNameChange = {},
            onInitialBalanceChange = {},
            onColorChange = {},
            onIconChange = {}
        )
    }
}
