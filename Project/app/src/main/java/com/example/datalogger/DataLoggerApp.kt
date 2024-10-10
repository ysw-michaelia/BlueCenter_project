package com.example.datalogger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.datalogger.data.PreferencesManager
import com.example.datalogger.screens.HomeScreen
import com.example.datalogger.screens.SetupScreen
import androidx.compose.runtime.mutableStateOf


@Composable
fun DataLoggerApp(
    modifier: Modifier = Modifier
) {
    //controller for navigation
    val navController = rememberNavController()

    //context needed for preferences (to know where to get them from)
    val context = LocalContext.current
    //create instance of preferences manager
    val preferencesManager = remember { PreferencesManager(context) }

    //create an external variable to check if setup is completed
    val isSetupCompleted = remember { mutableStateOf(preferencesManager.isSetupComplete()) }

    // Check if setup is completed and navigate accordingly
    NavHost(navController = navController, startDestination = if (isSetupCompleted.value) "home" else "setup") {
        composable("setup") { SetupScreen(navController, preferencesManager, isSetupCompleted) }
        composable("home") { HomeScreen(navController, preferencesManager, isSetupCompleted) }
    }
}