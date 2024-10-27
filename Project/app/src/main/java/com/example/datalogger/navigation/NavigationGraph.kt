package com.example.datalogger.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.datalogger.screens.ChannelSettingsScreen
import com.example.datalogger.screens.MasterBluetoothScreen
import com.example.datalogger.screens.MasterHomeScreen
import com.example.datalogger.screens.SetupScreen
import com.example.datalogger.screens.SlaveBluetoothScreen
import com.example.datalogger.screens.SlaveHomeScreen
import com.example.datalogger.state.BluetoothViewModel
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SensorViewModel
import com.example.datalogger.state.SetupViewModel

//navigation graph for the app
@Composable
fun NavGraph(
    navController: NavHostController,
    setupViewModel: SetupViewModel,
    channelViewModel: ChannelViewModel,
    bluetoothViewModel: BluetoothViewModel,
    sensorViewModel: SensorViewModel
) {
    //observe needed values
    val isSetupCompleted by setupViewModel.isSetupCompleted.observeAsState(false)
    val isMaster by setupViewModel.isMaster.observeAsState(false)



    //determine the start destination based on the setup
    val startDestination = when {
        !isSetupCompleted -> "setup"
        isSetupCompleted && isMaster -> "master_home"
        else -> "slave_home"
    }

    //setup the navhost for navigation
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("setup") {
            SetupScreen(navController = navController, viewModel = setupViewModel)
        }
        composable("master_home") {
            MasterHomeScreen(
                navController = navController,
                setupViewModel = setupViewModel,
                channelViewModel = channelViewModel,
                bluetoothViewModel = bluetoothViewModel
            )
        }
        composable("channel_settings/{channelId}") { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId")?.toInt()
            // Handle channelId if needed
            ChannelSettingsScreen(
                navController = navController,
                channelViewModel = channelViewModel,
                channelId = channelId!!
            )
        }
        composable("slave_home") {
            SlaveHomeScreen(
                navController = navController,
                setupViewModel = setupViewModel,
                channelViewModel = channelViewModel,
                bluetoothViewModel = bluetoothViewModel,
                sensorViewModel = sensorViewModel
            )
        }
        composable("master_bluetooth_settings") {
            MasterBluetoothScreen(
                navController = navController,
                setupViewModel = setupViewModel,
                channelViewModel = channelViewModel,
                bluetoothViewModel = bluetoothViewModel
            )
        }
        composable("slave_bluetooth_settings") {
            SlaveBluetoothScreen(
                navController = navController,
                setupViewModel = setupViewModel,
                channelViewModel = channelViewModel,
                bluetoothViewModel = bluetoothViewModel
            )
        }
        //future screens
    }
}
