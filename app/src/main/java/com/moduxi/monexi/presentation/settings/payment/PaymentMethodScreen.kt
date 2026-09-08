package com.moduxi.monexi.presentation.settings.payment

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    modifier: Modifier = Modifier,
    viewModel: PaymentMethodViewModel = viewModel()
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.newPaymentMethodName,
                onValueChange = onNewPaymentMethodNameChange,
                label = { Text("Novo Método de Pagamento") },
                modifier = Modifier.weight(1f)
            )

            Button (
                onClick = onAddPaymentMethodClick
            ) {
                Text("Adicionar")
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
                    PaymentMethod(id = 1, name = "Pix", isDefault = true),
                    PaymentMethod(id = 2, name = "Débito", isDefault = true),
                    PaymentMethod(id = 3, name = "Boleto", isDefault = false)
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
        )
    }
}