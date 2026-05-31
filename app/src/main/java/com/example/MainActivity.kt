package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthNavigator
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EcoScreen
import com.example.ui.viewmodel.EcoViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: EcoViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()

        if (currentScreen == EcoScreen.MAIN_DASHBOARD) {
          DashboardScreen(viewModel = viewModel)
        } else {
          AuthNavigator(viewModel = viewModel)
        }
      }
    }
  }
}
