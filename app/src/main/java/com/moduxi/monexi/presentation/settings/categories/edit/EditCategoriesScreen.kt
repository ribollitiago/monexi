package com.moduxi.monexi.presentation.settings.categories.edit

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.ui.theme.MonexiTheme

@Composable
fun EditCategoriesScreen(
    onCategorySaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditCategoriesViewModel = viewModel(factory = EditCategoriesViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    EditCategoriesContent(
        uiState = uiState,
        onTypeChange = viewModel::onTypeChange,
        onCategoryNameChange = viewModel::onNameChange,
        modifier = modifier,
        onSaveClick = {
            viewModel.saveCategory(
                onSaved = onCategorySaved
            )
        },
        onDeleteClick = {
            viewModel.deleteCategory(
                onDeleted = onCategorySaved
            )
        },
        onBackClick = onCategorySaved
    )
}

@Composable
fun EditCategoriesContent(
    uiState: EditCategoriesUiState,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryNameChange: (String) -> Unit,
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }

                    Text(
                        text = if (uiState.editingCategory == null) "Nova Categoria" else "Editar Categoria",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (uiState.editingCategory != null) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Categoria",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            item {
                val transactionTypes = listOf(
                    TransactionType.EXPENSE to "Despesa",
                    TransactionType.INCOME to "Receita"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
            }
            item {
                OutlinedTextField(
                    value = uiState.categoryName,
                    onValueChange = onCategoryNameChange,
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
fun EditCategoriesPreview() {
    MonexiTheme {
        EditCategoriesContent(
            uiState = EditCategoriesUiState(),
            onSaveClick = {},
            onDeleteClick = {},
            onBackClick = {},
            onCategoryNameChange = {},
            onTypeChange = {}
        )
    }
}