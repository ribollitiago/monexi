package com.moduxi.monexi.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.moduxi.monexi.MonexiApplication
import com.moduxi.monexi.data.repository.local.ThemeManager
import com.moduxi.monexi.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.moduxi.monexi.presentation.auth.login.LoginScreen
import com.moduxi.monexi.presentation.auth.register.RegisterScreen
import com.moduxi.monexi.presentation.extract.ExtractScreen
import com.moduxi.monexi.presentation.onboarding.OnboardingScreen
import com.moduxi.monexi.presentation.settings.SettingsScreen
import com.moduxi.monexi.presentation.settings.accounts.BankAccountScreen
import com.moduxi.monexi.presentation.settings.accounts.archived.ArchivedBankAccountScreen
import com.moduxi.monexi.presentation.settings.accounts.edit.EditBankAccountScreen
import com.moduxi.monexi.presentation.settings.accounts.statement.BankAccountStatementScreen
import com.moduxi.monexi.presentation.settings.categories.CategoriesScreen
import com.moduxi.monexi.presentation.settings.categories.archived.ArchivedCategoriesScreen
import com.moduxi.monexi.presentation.settings.categories.edit.EditCategoriesScreen
import com.moduxi.monexi.presentation.settings.credit.CreditCardScreen
import com.moduxi.monexi.presentation.settings.credit.archived.ArchivedCreditCardScreen
import com.moduxi.monexi.presentation.settings.credit.bills.CreditCardBillDetailScreen
import com.moduxi.monexi.presentation.settings.credit.bills.CreditCardBillsScreen
import com.moduxi.monexi.presentation.settings.credit.edit.EditCreditCardScreen
import com.moduxi.monexi.presentation.settings.recurring.RecurringTransactionsScreen
import kotlinx.coroutines.flow.first

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

    val startDestination = "splash"

    val showBottomBar = currentRoute in listOf(
        "home",
        "extract",
        "transaction",
        "transaction?id={id}",
        "settings",
        "categories",
        "archivedCategories",
        "editCategory",
        "creditCards",
        "archivedCreditCard",
        "editCreditCard",
        "bankAccounts",
        "archivedBankAccount",
        "editBankAccount",
        "bankAccountStatement?accountId={accountId}",
        "recurring"
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
                                "settings" -> currentRoute == "settings" || currentRoute == "categories" || currentRoute == "bankAccounts" || currentRoute == "recurring" || currentRoute?.startsWith("bankAccountStatement") == true
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
            composable("splash") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                LaunchedEffect(Unit) {
                    val user = app.authRepository.currentUser
                    if (user == null) {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        val pending = app.onboardingPrefs.isPending(user.uid).first()
                        if (pending) {
                            navController.navigate("onboarding") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true}
                            }
                        }
                    }
                }
            }
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate("forgotPassword")
                    }
                )
            }
            composable("forgotPassword") {
                ForgotPasswordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("onboarding") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable("onboarding") {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
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
                        navController.popBackStack()
                    }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateToCategories = {
                        navController.navigate("categories")
                    },
                    onNavigateToBankAccount = {
                        navController.navigate("bankAccounts")
                    },
                    onNavigateToCreditCard = {
                        navController.navigate("creditCards")
                    },
                    onNavigateToRecurring = {
                        navController.navigate("recurring")
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
            composable("archivedCategories") {
                ArchivedCategoriesScreen(
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
            composable(route = "creditCards") {
                CreditCardScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateArchived = {
                        navController.navigate("archivedCreditCard")
                    },
                    onNavigateNew = {
                        navController.navigate("editCreditCard")
                    },
                    onNavigateBills = { cardId ->
                        navController.navigate("creditCardBills?cardId=$cardId")
                    }
                )
            }
            composable(
                route = "creditCardBills?cardId={cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
            ) {
                CreditCardBillsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateEdit = { cardId ->
                        navController.navigate("editCreditCard?id=$cardId")
                    },
                    onNavigateToBillDetail = { cardId, month, year ->
                        navController.navigate("creditCardBillDetail?cardId=$cardId&month=$month&year=$year")
                    }
                )
            }
            composable(
                route = "creditCardBillDetail?cardId={cardId}&month={month}&year={year}",
                arguments = listOf(
                    navArgument("cardId") { type = NavType.StringType },
                    navArgument("month") { type = NavType.IntType },
                    navArgument("year") { type = NavType.IntType }
                )
            ) {
                CreditCardBillDetailScreen(
                    month = it.arguments?.getInt("month") ?: 1,
                    year = it.arguments?.getInt("year") ?: 2026,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "editCreditCard?id={id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                EditCreditCardScreen(
                    onCreditCardSaved = {
                        navController.popBackStack()
                    }
                )
            }
            composable("archivedCreditCard") {
                ArchivedCreditCardScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("bankAccounts") {
                BankAccountScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateArchived = {
                        navController.navigate("archivedBankAccount")
                    },
                    onNavigateNew = {
                        navController.navigate("editBankAccount")
                    },
                    onNavigateStatement = { accountId ->
                        navController.navigate("bankAccountStatement?accountId=$accountId")
                    }
                )
            }
            composable(
                route = "bankAccountStatement?accountId={accountId}",
                arguments = listOf(
                    navArgument("accountId") {
                        type = NavType.StringType
                    }
                )
            ) {
                BankAccountStatementScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateEdit = { accountId ->
                        navController.navigate("editBankAccount?id=$accountId")
                    },
                    onNavigateToTransaction = { transactionId ->
                        navController.navigate("transaction?id=$transactionId")
                    }
                )
            }
            composable(
                route = "editBankAccount?id={id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                EditBankAccountScreen(
                    onBankAccountSaved = {
                        navController.popBackStack()
                    }
                )
            }
            composable("archivedBankAccount") {
                ArchivedBankAccountScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("recurring") {
                RecurringTransactionsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
