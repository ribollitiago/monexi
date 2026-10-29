package com.moduxi.monexi.presentation.settings.payment.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.ui.theme.MonexiTheme

@Composable
fun EditPaymentMethodScreen(
    onPaymentMethodSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPaymentMethodViewModel = viewModel(factory = EditPaymentMethodViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    EditPaymentMethodContent(
        uiState = uiState,
        onPaymentMethodNameChange = viewModel::onNameChange,
        modifier = modifier,
        onSaveClick = {
            viewModel.savePaymentMethod(
                onSaved = onPaymentMethodSaved
            )
        },
        onDeleteClick = {
            viewModel.deletePaymentMethod(
                onDeleted = onPaymentMethodSaved
            )
        },
        onBackClick = onPaymentMethodSaved
    )
}

@Composable
fun EditPaymentMethodContent(
    uiState: EditPaymentMethodUiState,
    onPaymentMethodNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {
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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }

                    Text(
                        text = if (uiState.editingPaymentMethod == null) "Novo Método" else "Editar Método",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (uiState.editingPaymentMethod != null && !uiState.editingPaymentMethod.isDefault) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Método",
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
                    value = uiState.paymentMethodName,
                    onValueChange = onPaymentMethodNameChange,
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
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
fun EditPaymentMethodPreview() {
    MonexiTheme {
        EditPaymentMethodContent(
            uiState = EditPaymentMethodUiState(),
            onSaveClick = {},
            onDeleteClick = {},
            onBackClick = {},
            onPaymentMethodNameChange = {}
        )
    }
}
