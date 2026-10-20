package com.moduxi.monexi.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun String.toComposeColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color(0xFF1B8A5A)
    }
}

fun String.toCategoryIcon(): ImageVector {
    return when (this) {
        "Restaurant" -> Icons.Default.Restaurant
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "Home" -> Icons.Default.Home
        "MedicalServices" -> Icons.Default.MedicalServices
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "Work" -> Icons.Default.Work
        "AttachMoney" -> Icons.Default.AttachMoney
        else -> Icons.Default.Category
    }
}