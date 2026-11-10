package com.moduxi.monexi.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moduxi.monexi.domain.model.account.AccountType
import com.moduxi.monexi.ui.components.DropdownField
import com.moduxi.monexi.ui.theme.CurrencyVisualTransformation
import com.moduxi.monexi.ui.theme.toComposeColor

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(24.dp)
        ) {
            Text(
                text = "Etapa ${uiState.step + 1} de 3",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            LinearProgressIndicator(
                progress = { (uiState.step + 1) / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState.step) {
                    0 -> BankStep(uiState.bank, viewModel::updateBank)
                    1 -> CardStep(uiState.card, viewModel::updateCard)
                    else -> CategoriesStep(
                        categories = uiState.categories,
                        newName = uiState.newCategoryName,
                        newColor = uiState.newCategoryColor,
                        onNameChange = viewModel::onNewCategoryNameChange,
                        onColorChange = viewModel::onNewCategoryColorChange,
                        onAddCategory = viewModel::addCategory,
                        onRemoveCategory = viewModel::removeCategory
                    )
                }

                uiState.error?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.step > 0) {
                    OutlinedButton(onClick = viewModel::back) {
                        Text("Voltar")
                    }
                }

                if (uiState.step > 0) {
                    TextButton(onClick = { viewModel.next(onFinished) }) {
                        Text("Pular")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    enabled = !uiState.isSaving,
                    onClick = { viewModel.next(onFinished) }
                ) {
                    Text(if (uiState.step == 2) "Concluir" else "Continuar")
                }
            }
        }
    }
}

@Composable
private fun BankStep(b: BankDraft, onChange: (BankDraft) -> Unit) {
    val availableColors = listOf("#1B8A5A", "#1565C0", "#C62828", "#F57C00", "#6A1B9A", "#00838F", "#D81B60", "#424242")

    Text(text = "Sua conta principal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(text = "Cadastre sua principal conta ou banco para começar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

    OutlinedTextField(
        value = b.name,
        onValueChange = { onChange(b.copy(name = it)) },
        label = { Text("Nome do Banco / Conta (ex: Nubank)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    DropdownField(
        label = "Tipo de Conta",
        options = AccountType.entries,
        selectedOption = b.type,
        onOptionSelected = { onChange(b.copy(type = it)) },
        optionLabel = { it.label }
    )

    OutlinedTextField(
        value = b.balanceDigits,
        onValueChange = { onChange(b.copy(balanceDigits = it.filter(Char::isDigit).take(12))) },
        label = { Text("Saldo Atual (R$)") },
        placeholder = { Text("0,00") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = CurrencyVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )

    Text(text = "Cor da Conta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(availableColors) { colorHex ->
            val isSelected = b.colorHex == colorHex
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colorHex.toComposeColor())
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onChange(b.copy(colorHex = colorHex)) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CardStep(c: CardDraft, onChange: (CardDraft) -> Unit) {
    val availableColors = listOf("#1565C0", "#1B8A5A", "#C62828", "#F57C00", "#6A1B9A", "#00838F", "#D81B60", "#424242")

    Text(text = "Cartão de Crédito (Opcional)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(text = "Se você utiliza cartão de crédito, cadastre-o agora ou clique em Pular.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

    OutlinedTextField(
        value = c.name,
        onValueChange = { onChange(c.copy(name = it)) },
        label = { Text("Nome do Cartão (ex: Nubank Gold)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = c.limitDigits,
        onValueChange = { onChange(c.copy(limitDigits = it.filter(Char::isDigit).take(12))) },
        label = { Text("Limite do Cartão (R$)") },
        placeholder = { Text("0,00") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = CurrencyVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = c.closingDay,
            onValueChange = { onChange(c.copy(closingDay = it.filter(Char::isDigit).take(2))) },
            label = { Text("Dia Fechamento") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = c.dueDay,
            onValueChange = { onChange(c.copy(dueDay = it.filter(Char::isDigit).take(2))) },
            label = { Text("Dia Vencimento") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }

    Text(text = "Cor do Cartão", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(availableColors) { colorHex ->
            val isSelected = c.colorHex == colorHex
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colorHex.toComposeColor())
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onChange(c.copy(colorHex = colorHex)) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoriesStep(
    categories: List<CategoryDraft>,
    newName: String,
    newColor: String,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onAddCategory: () -> Unit,
    onRemoveCategory: (Int) -> Unit
) {
    val availableColors = listOf("#E53935", "#1E88E5", "#8E24AA", "#00ACC1", "#43A047", "#FB8C00", "#3949AB", "#D81B60")

    Text(text = "Categorias Personalizadas (Opcional)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(text = "Adicione suas próprias categorias de gastos ou pule para usar as padrão.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newName,
            onValueChange = onNameChange,
            label = { Text("Nome da Categoria (ex: Pets)") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onAddCategory,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text("Adicionar")
        }
    }

    Text(text = "Cor da Categoria", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(availableColors) { colorHex ->
            val isSelected = newColor == colorHex
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorHex.toComposeColor())
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorChange(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (categories.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Categorias Adicionadas:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEachIndexed { index, cat ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(cat.colorHex.toComposeColor())
                            )
                            Text(text = cat.name, fontWeight = FontWeight.Medium)
                        }

                        IconButton(onClick = { onRemoveCategory(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
