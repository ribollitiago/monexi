package com.moduxi.monexi.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payment
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.data.repository.local.ThemeManager
import com.moduxi.monexi.ui.theme.MonexiTheme

@Composable
fun SettingsScreen (
    themeManager: ThemeManager,
    modifier: Modifier = Modifier,
){
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(themeManager)
    )
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { SettingsSectionTitle("Personalização") }
        item {
            SettingsItem(
                title = "Categorias",
                subtitle = "Gerencie suas categorias de gastos",
                icon = Icons.Default.Category,
                onClick = { /* Navegar para tela de categorias */ }
            )
        }
        item {
            SettingsItem(
                title = "Métodos de Pagamento",
                subtitle = "Cartão, Dinheiro, Pix",
                icon = Icons.Default.Payment,
                onClick = {}
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
        item {
            SettingsItem(
                title = "Sair",
                icon = Icons.Default.Logout,
                onClick = { viewModel.logout() }
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
            ThemeManager(LocalContext.current)
        )
    }
}

class SettingsViewModelFactory(private val themeManager: ThemeManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(themeManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}