package com.moduxi.monexi.presentation.settings.categories

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
        onTypeChange = viewModel::onTypeChange,
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
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTypeChange: (TransactionType) -> Unit
) {
    val visibleCategories = uiState.categories.filter { category ->
        category.type == uiState.selectedType
    }

    Column (
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
                text = "Categorias",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val transactionTypes = listOf(
                TransactionType.INCOME to "Receita",
                TransactionType.EXPENSE to "Despesa"
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
            items(visibleCategories) { category ->
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
                    Category(
                        id = 1,
                        name = "Alimentacao",
                        type = TransactionType.EXPENSE,
                        isDefault = true
                    ),
                    Category(
                        id = 2,
                        name = "Transporte",
                        TransactionType.EXPENSE,
                        isDefault = true
                    ),
                    Category(id = 3, name = "Viagem", TransactionType.EXPENSE, isDefault = false)
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
            onTypeChange = {}
        )
    }
}
