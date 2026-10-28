package com.moduxi.monexi.presentation.settings.payment

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.ui.theme.MonexiTheme

@Composable
fun PaymentMethodScreen(
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    onNavigateEdit: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentMethodViewModel = viewModel(factory = PaymentMethodViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    PaymentMethodContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateArchived = onNavigateArchived,
        onNavigateEdit = { onNavigateEdit(null) },
        onPaymentMethodClick = { paymentMethod -> onNavigateEdit(paymentMethod.id) },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun PaymentMethodContent(
    uiState: PaymentMethodUiState,
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    onNavigateEdit: () -> Unit,
    onPaymentMethodClick: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
                Text(
                    text = "Métodos de Pagamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNavigateArchived) {
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
                        onClick = { onPaymentMethodClick(paymentMethod) }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { onNavigateEdit() },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Adicionar novo") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun PaymentMethodItem(
    paymentMethod: PaymentMethod,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = paymentMethod.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (paymentMethod.isDefault) "Padrão" else "Criada por você",
                style = MaterialTheme.typography.bodySmall
            )
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
            onNavigateBack = {},
            onNavigateArchived = {},
            onNavigateEdit = {},
            onPaymentMethodClick = {}
        )
    }
}
