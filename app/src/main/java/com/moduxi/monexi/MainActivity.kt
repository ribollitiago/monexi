package com.moduxi.monexi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.moduxi.monexi.data.repository.local.ThemeManager
import com.moduxi.monexi.presentation.navigation.AppNavigation
import com.moduxi.monexi.ui.theme.MonexiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeManager = ThemeManager(applicationContext)

        setContent {
            val isDarkTheme by themeManager.isDarkMode.collectAsState(initial = false)

            MonexiTheme(darkTheme = isDarkTheme) {
                AppNavigation(themeManager = themeManager)
            }
        }
    }
}