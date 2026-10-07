package com.moduxi.monexi.presentation.settings.categories

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.ui.theme.MonexiTheme

@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    onNavigateEdit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    CategoriesContent(
        uiState = uiState,
        onNewCategoryNameChange = viewModel::onNewCategoryNameChange,
        onAddCategoryClick = viewModel::addCategory,
        onEditCategoryClick = viewModel::startEditing,
        onDeleteCategoryClick = viewModel::deleteCategory,
        onEditingCategoryNameChange = viewModel::onEditingCategoryNameChange,
        onSaveEditingClick = viewModel::saveEditing,
        onCancelEditingClick = viewModel::cancelEditing,
        onNavigateBack = onNavigateBack,
        onNavigateArchived = onNavigateArchived,
        onNavigateEdit = onNavigateEdit,
        onTypeChange = viewModel::onTypeChange,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun CategoriesContent(
    uiState: CategoriesUiState,
    onNewCategoryNameChange: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Category) -> Unit,
    onDeleteCategoryClick: (Category) -> Unit,
    onEditingCategoryNameChange: (String) -> Unit,
    onSaveEditingClick: () -> Unit,
    onCancelEditingClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateArchived: () -> Unit,
    onNavigateEdit: () -> Unit,
    modifier: Modifier = Modifier,
    onTypeChange: (TransactionType) -> Unit
) {
    val visibleCategories = uiState.categories.filter { category ->
        category.type == uiState.selectedType
    }

    var showAddDialog by remember { mutableStateOf(false) }

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
                Text(
                    text = "Categorias",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val transactionTypes = listOf(
                    TransactionType.EXPENSE to "Despesa",
                    TransactionType.INCOME to "Receita"
                )

                transactionTypes.forEach { (type, label) ->
                    val isSelected = uiState.selectedType == type

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
                items(visibleCategories) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onEditCategoryClick(category) }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { onNavigateEdit },
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
            title = { Text("Nova Categoria") },
            text = {
                OutlinedTextField(
                    value = uiState.newCategoryName,
                    onValueChange = onNewCategoryNameChange,
                    label = { Text("Nome da Categoria") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddCategoryClick()
                        showAddDialog = false
                        onNewCategoryNameChange("") // Limpa o campo opcionalmente
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
private fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (category.isDefault) "Padrão" else "Criada por você",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoriesScreenPreview() {
    MonexiTheme {
        CategoriesContent(
            uiState = CategoriesUiState(
                categories = listOf(
                    Category(
                        id = "1",
                        userId = "1",
                        name = "Alimentacao",
                        type = TransactionType.EXPENSE,
                        isDefault = true
                    ),
                    Category(
                        id = "2",
                        userId = "1",
                        name = "Transporte",
                        type = TransactionType.EXPENSE,
                        isDefault = true
                    ),
                    Category(
                        id = "3",
                        userId = "1",
                        name = "Viagem",
                        type = TransactionType.EXPENSE,
                        isDefault = false
                    ),
                    Category(
                        id = "3",
                        userId = "1",
                        name = "Viagem",
                        type = TransactionType.EXPENSE,
                        isDefault = false
                    ),
                    Category(
                        id = "3",
                        userId = "1",
                        name = "Viagem",
                        type = TransactionType.EXPENSE,
                        isDefault = false
                    ),
                    Category(
                        id = "3",
                        userId = "1",
                        name = "Viagem",
                        type = TransactionType.EXPENSE,
                        isDefault = false
                    ),
                    Category(
                        id = "3",
                        userId = "1",
                        name = "Viagem",
                        type = TransactionType.EXPENSE,
                        isDefault = false
                    )
                )
            ),
            onNewCategoryNameChange = {},
            onAddCategoryClick = {},
            onEditCategoryClick = {},
            onDeleteCategoryClick = {},
            onEditingCategoryNameChange = {},
            onSaveEditingClick = {},
            onCancelEditingClick = {},
            onNavigateBack = {},
            onNavigateArchived = {},
            onNavigateEdit = {},
            onTypeChange = {}
        )
    }
}
