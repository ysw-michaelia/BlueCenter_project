package com.example.datalogger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.datalogger.screens.ChannelSettingsScreen
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SetupViewModel


@Composable
fun DataLoggerApp(
    modifier: Modifier = Modifier
) {
    //controller for navigation
    val navController = rememberNavController()

    //initialize needed viewModels and the variable that checks if setup is completed
    val setupViewModel: SetupViewModel = viewModel()
    val channelViewModel: ChannelViewModel = viewModel()
    val isSetupCompleted by setupViewModel.isSetupCompleted.observeAsState(false)

    //check if setup is completed and navigate accordingly
    NavHost(navController = navController, startDestination = if (isSetupCompleted) "home" else "setup") {
        //goes to setup screen, if not completed
        composable("setup") { SetupScreen(navController, setupViewModel ) }
        //goes to home screen, if setup completed
        composable("home") { HomeScreen(navController, setupViewModel, channelViewModel) }
        //goes to individual channel settings screen
        composable(
            "channel_settings/{channelId}",
                arguments = listOf(navArgument("channelId") { type = NavType.IntType })
            ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getInt("channelId")
            ChannelSettingsScreen(
                navController = navController,
                channelId = channelId!!,
                channelViewModel = channelViewModel
            )

        }

    }
}