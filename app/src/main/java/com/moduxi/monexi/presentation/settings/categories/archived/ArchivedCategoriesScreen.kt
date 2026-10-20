package com.moduxi.monexi.presentation.settings.categories.archived

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType
import com.moduxi.monexi.presentation.settings.categories.CategoriesUiState
import com.moduxi.monexi.ui.theme.MonexiTheme
import com.moduxi.monexi.ui.theme.toCategoryIcon
import com.moduxi.monexi.ui.theme.toComposeColor

@Composable
fun ArchivedCategoriesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchivedCategoriesViewModel = viewModel(factory = ArchivedCategoriesViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    ArchivedCategoriesContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUnarchiveCategoryClick = viewModel::unarchivedClick,
        modifier = modifier
    )
}

@Composable
fun ArchivedCategoriesContent(
    uiState: ArchivedCategoriesUiState,
    onNavigateBack: () -> Unit,
    onUnarchiveCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
            Text(
                text = "Categorias Arquivadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        if (uiState.categoriesArchived.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma categoria arquivada.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categoriesArchived, key = { it.id }) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(category.colorHex.toComposeColor().copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.iconName.toCategoryIcon(),
                                    contentDescription = category.name,
                                    tint = category.colorHex.toComposeColor()
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (category.type.name == "INCOME") "Receita" else "Despesa",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            IconButton(
                                onClick = { onUnarchiveCategoryClick(category) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Unarchive,
                                    contentDescription = "Desarquivar"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchivedCategoriesScreenPreview() {
    MonexiTheme() {
        ArchivedCategoriesContent(
            uiState = ArchivedCategoriesUiState(
                categoriesArchived = listOf(
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
                        type = TransactionType.INCOME,
                        isDefault = false
                    )
                )
            ),
            onNavigateBack = {},
            onUnarchiveCategoryClick = {}
        )
    }
}