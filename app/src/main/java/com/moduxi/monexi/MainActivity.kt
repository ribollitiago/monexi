package com.moduxi.monexi

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.moduxi.monexi.presentation.navigation.AppNavigation
import com.moduxi.monexi.ui.theme.MonexiTheme

class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            viewModel.isDarkMode.value == null
        }

        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsState()
            val themeManager = (application as MonexiApplication).themeManager

            MonexiTheme(darkTheme = isDarkTheme ?: isSystemInDarkTheme()) {
                AppNavigation(themeManager = themeManager)
            }
        }
    }
}
