package com.moduxi.monexi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moduxi.monexi.presentation.home.HomeScreen
import com.moduxi.monexi.presentation.transaction.TransactionScreen
import com.moduxi.monexi.ui.theme.MonexiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonexiTheme {
                Scaffold { innerPadding ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToTransaction = {
                                    navController.navigate("transaction")
                                }
                            )
                        }
                        composable("transaction") {
                            TransactionScreen()
                        }
                    }
                }
            }
        }
    }
}
