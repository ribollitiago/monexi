package com.moduxi.monexi.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moduxi.monexi.presentation.home.HomeScreen
import com.moduxi.monexi.presentation.transaction.TransactionScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.data.repository.local.ThemeManager
import com.moduxi.monexi.presentation.auth.login.LoginScreen
import com.moduxi.monexi.presentation.auth.register.RegisterScreen
import com.moduxi.monexi.presentation.extract.ExtractScreen
import com.moduxi.monexi.presentation.settings.SettingsScreen
import com.moduxi.monexi.presentation.settings.categories.CategoriesScreen
import com.moduxi.monexi.presentation.settings.categories.archived.ArchivedCategoriesScreen
import com.moduxi.monexi.presentation.settings.categories.edit.EditCategoriesScreen
import com.moduxi.monexi.presentation.settings.payment.PaymentMethodScreen
import com.moduxi.monexi.presentation.settings.payment.archived.ArchivedPaymentMethodScreen

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Resumo", Icons.Default.Home)
    object Extract : BottomNavItem("extract", "Extrato", Icons.Default.ListAlt)
    object Transaction : BottomNavItem("transaction", "Resumo", Icons.Default.Add)
    object Settings : BottomNavItem("settings", "Configurações", Icons.Default.Person)
}

@Composable
fun AppNavigation(themeManager: ThemeManager) {
    val context = LocalContext.current
    val app = context.applicationContext as MonexiApplication
    val authRepository = app.authRepository

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = if (authRepository.currentUser != null) "home" else "login"

    val showBottomBar = currentRoute in listOf(
        "home",
        "extract",
        "transaction",
        "transaction?id={id}",
        "settings",
        "categories",
        "paymentMethods",
        "archivedCategories",
        "archivedPaymentMethod",
        "editCategory"
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Extract,
                        BottomNavItem.Transaction,
                        BottomNavItem.Settings
                    )
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = when (item.route) {
                                "settings" -> currentRoute == "settings" || currentRoute == "categories" || currentRoute == "paymentMethods"
                                "transaction" -> currentRoute?.startsWith("transaction") == true
                                else -> currentRoute == item.route
                            },
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo("home") {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable("home") {
                HomeScreen()
            }
            composable("extract") {
                ExtractScreen(
                    onNavigateToTransaction = { id ->
                        if (id != null) {
                            navController.navigate("transaction?id=$id")
                        } else {
                            navController.navigate("transaction")
                        }
                    }
                )
            }
            composable(route = "transaction?id={id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                TransactionScreen(
                    onTransactionSaved = {
                        // Se for uma barra de navegação, o popBackStack vai voltar para a Home
                        navController.popBackStack()
                    }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateToCategories = {
                        navController.navigate("categories")
                    },
                    onNavigateToPaymentMethods = {
                        navController.navigate("paymentMethods")
                    },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                )
            }
            composable("categories") {
                CategoriesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateArchived = {
                        navController.navigate("archivedCategories")
                    },
                    onNavigateEdit = { id ->
                        if (id != null) {
                            navController.navigate("editCategory?id=$id")
                        } else {
                            navController.navigate("editCategory")
                        }
                    }
                )
            }
            composable ("paymentMethods") {
                PaymentMethodScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateArchived = {
                        navController.navigate("archivedPaymentMethod")
                    }
                )
            }
            composable("archivedCategories") {
                ArchivedCategoriesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("archivedPaymentMethod") {
                ArchivedPaymentMethodScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "editCategory?id={id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                EditCategoriesScreen(
                    onCategorySaved = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}