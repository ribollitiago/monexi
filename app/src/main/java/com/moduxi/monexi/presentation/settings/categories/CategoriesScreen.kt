package com.moduxi.monexi.presentation.settings.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.ui.theme.MonexiTheme

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = viewModel()
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
        modifier = modifier
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
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
       Text (
           text = "Categorias",
           style = MaterialTheme.typography.headlineMedium,
           fontWeight = FontWeight.Bold
       )

        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.newCategoryName,
                onValueChange = onNewCategoryNameChange,
                label = { Text("Nova Categoria") },
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onAddCategoryClick
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
            items(uiState.categories) { category ->
                CategoryItem(
                    category = category,
                    isEditing = uiState.editingCategory?.id == category.id,
                    editingName = uiState.editingCategoryName,
                    onEditClick = { onEditCategoryClick(category) },
                    onDeleteClick = { onDeleteCategoryClick(category) },
                    onEditingNameChange = onEditingCategoryNameChange,
                    onSaveEditingClick = onSaveEditingClick,
                    onCancelEditingClick = onCancelEditingClick
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
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
                    label = { Text("Editar categoria") },
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
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (category.isDefault) "Padrao" else "Criada por voce",
                    style = MaterialTheme.typography.bodySmall
                )

                if (!category.isDefault) {
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
private fun CategoriesScreenPreview() {
    MonexiTheme {
        CategoriesContent(
            uiState = CategoriesUiState(
                categories = listOf(
                    Category(id = 1, name = "Alimentacao", isDefault = true),
                    Category(id = 2, name = "Transporte", isDefault = true),
                    Category(id = 3, name = "Viagem", isDefault = false)
                )
            ),
            onNewCategoryNameChange = {},
            onAddCategoryClick = {},
            onEditCategoryClick = {},
            onDeleteCategoryClick = {},
            onEditingCategoryNameChange = {},
            onSaveEditingClick = {},
            onCancelEditingClick = {}
        )
    }
}