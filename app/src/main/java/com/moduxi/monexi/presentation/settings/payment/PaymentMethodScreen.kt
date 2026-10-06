package com.moduxi.monexi.presentation.settings.payment

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.ui.theme.MonexiTheme

@Composable
fun PaymentMethodScreen(
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentMethodViewModel = viewModel(factory = PaymentMethodViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    PaymentMethodContent(
        uiState = uiState,
        onNewPaymentMethodNameChange = viewModel::onNewPaymentMethodNameChange,
        onAddPaymentMethodClick = viewModel::addPaymentMethod,
        onEditPaymentMethodClick = viewModel::startEditing,
        onDeletePaymentMethodClick = viewModel::deletePaymentMethod,
        onEditingPaymentMethodNameChange = viewModel::onEditingPaymentMethodChange,
        onSaveEditingClick = viewModel::saveEditing,
        onCancelEditingClick = viewModel::cancelEditing,
        onNavigateBack = onNavigateBack,
        onNavigateArchived = onNavigateArchived,
        modifier = modifier
    )
}

@Composable
private fun PaymentMethodContent(
    uiState: PaymentMethodUiState,
    onNewPaymentMethodNameChange: (String) -> Unit,
    onAddPaymentMethodClick: () -> Unit,
    onEditPaymentMethodClick: (PaymentMethod) -> Unit,
    onDeletePaymentMethodClick: (PaymentMethod) -> Unit,
    onEditingPaymentMethodNameChange: (String) -> Unit,
    onSaveEditingClick: () -> Unit,
    onCancelEditingClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Box (
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
                Text (
                    text = "Métodos de Pagamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onNavigateArchived
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Arquivados"
                    )
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.paymentMethods) { paymentMethod ->
                    PaymentMethodItem(
                        paymentMethod = paymentMethod,
                        isEditing = uiState.editingPaymentMethod?.id == paymentMethod.id,
                        editingName = uiState.editingPaymentMethodName,
                        onEditClick = { onEditPaymentMethodClick(paymentMethod) },
                        onDeleteClick = { onDeletePaymentMethodClick(paymentMethod) },
                        onEditingNameChange = onEditingPaymentMethodNameChange,
                        onSaveEditingClick = onSaveEditingClick,
                        onCancelEditingClick = onCancelEditingClick
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Adicionar novo") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Novo Método") },
            text = {
                OutlinedTextField(
                    value = uiState.newPaymentMethodName,
                    onValueChange = onNewPaymentMethodNameChange,
                    label = { Text("Nome do Método") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddPaymentMethodClick()
                        showAddDialog = false
                        onNewPaymentMethodNameChange("") // Limpa o campo opcionalmente
                    }
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PaymentMethodItem(
   paymentMethod: PaymentMethod,
   isEditing: Boolean,
   editingName: String,
   onEditClick: () -> Unit,
   onDeleteClick: () -> Unit,
   onEditingNameChange: (String) -> Unit,
   onSaveEditingClick: () -> Unit,
   onCancelEditingClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = onEditingNameChange,
                    label = { Text("Editar método de pagamento") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onSaveEditingClick) {
                        Text("Salvar")
                    }

                    TextButton(onClick = onCancelEditingClick) {
                        Text("Cancelar")
                    }
                }
            } else {
                Text(
                    text = paymentMethod.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (paymentMethod.isDefault) "Padrao" else "Criada por voce",
                    style = MaterialTheme.typography.bodySmall
                )

                if (!paymentMethod.isDefault) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = onEditClick) {
                            Text("Editar")
                        }

                        TextButton(onClick = onDeleteClick) {
                            Text("Excluir")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentMethodScreenPreview() {
    MonexiTheme {
        PaymentMethodContent(
            uiState = PaymentMethodUiState(
                paymentMethods = listOf(
                    PaymentMethod(id = "1", name = "Pix", isDefault = true),
                    PaymentMethod(id = "2", name = "Débito", isDefault = true),
                    PaymentMethod(id = "3", name = "Boleto", isDefault = false)
                )
            ),
            onNewPaymentMethodNameChange = {},
            onAddPaymentMethodClick = {},
            onEditPaymentMethodClick = {},
            onDeletePaymentMethodClick = {},
            onEditingPaymentMethodNameChange = {},
            onSaveEditingClick = {},
            onCancelEditingClick = {},
            onNavigateBack = {},
            onNavigateArchived = {}
        )
    }
}