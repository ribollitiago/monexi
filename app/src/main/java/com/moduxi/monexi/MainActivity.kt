package com.moduxi.monexi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moduxi.monexi.presentation.navigation.AppNavigation // Importação necessária
import com.moduxi.monexi.ui.theme.MonexiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonexiTheme {
                AppNavigation()
            }
        }
    }
}