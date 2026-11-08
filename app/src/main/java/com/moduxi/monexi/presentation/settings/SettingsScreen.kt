package com.moduxi.monexi.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.ui.theme.MonexiTheme

@Composable
fun SettingsScreen (
    onNavigateToCategories: () -> Unit,
    onNavigateToBankAccount: () -> Unit,
    onNavigateToCreditCard: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
){
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory
    )
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { SettingsSectionTitle("Conta") }
        item {
            SettingsItem(
                title = "Contas Bancárias",
                subtitle = "Gerencie suas contas",
                icon = Icons.Default.MonetizationOn,
                onClick = onNavigateToBankAccount
            )
        }
        item {
            SettingsItem(
                title = "Cartões de Crédito",
                subtitle = "Gerencie seus cartões de crédito",
                icon = Icons.Default.CreditCard,
                onClick = onNavigateToCreditCard
            )
        }
        item { SettingsSectionTitle("Personalização") }
        item {
            SettingsItem(
                title = "Categorias",
                subtitle = "Gerencie suas categorias de gastos",
                icon = Icons.Default.Category,
                onClick = onNavigateToCategories
            )
        }
        item {
            SettingsItem(
                title = "Lançamentos Recorrentes",
                subtitle = "Assinaturas e contas fixas mensais",
                icon = Icons.Default.Repeat,
                onClick = onNavigateToRecurring
            )
        }
        item { HorizontalDivider() }

        item { SettingsSectionTitle("Preferências") }
        item {
            SettingsItem(
                title = "Tema do App",
                icon = Icons.Default.Brightness6,
                onClick = {
                    viewModel.toggleTheme(!uiState.isDarkTheme)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.isDarkTheme,
                        onCheckedChange = { viewModel.toggleTheme(it) }
                    )
                }
            )
        }

        item { HorizontalDivider() }
        item { SettingsSectionTitle("Sua Conta") }
        uiState.userEmail?.let { email ->
            item {
                Text(
                    text = "Logado como $email",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
        item {
            SettingsItem(
                title = "Sair",
                icon = Icons.Default.Logout,
                onClick = { viewModel.logout(onLogoutSuccess = onLogout) }
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null )},
        trailingContent = trailingContent,
        modifier =  Modifier.clickable{ onClick() }
    )
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MonexiTheme {
        SettingsScreen(
            onNavigateToCategories = {},
            onNavigateToCreditCard = {},
            onNavigateToBankAccount = {},
            onNavigateToRecurring = {},
            onLogout = {}
        )
    }
}
