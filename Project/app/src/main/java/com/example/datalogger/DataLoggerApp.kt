package com.example.datalogger

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.datalogger.navigation.NavGraph
import com.example.datalogger.state.BluetoothViewModel
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SensorViewModel
import com.example.datalogger.state.SetupViewModel


@Composable
fun DataLoggerApp(
    bluetoothViewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    //controller for navigation
    val navController = rememberNavController()

    //initialize needed viewModels and the variable that checks if setup is completed
    val setupViewModel: SetupViewModel = viewModel()
    val sensorViewModel: SensorViewModel = viewModel()
    val channelViewModel: ChannelViewModel = viewModel()

    NavGraph(navController, setupViewModel, channelViewModel, bluetoothViewModel, sensorViewModel)
}