package com.example.datalogger.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datalogger.state.BluetoothUiState
import com.example.datalogger.state.BluetoothViewModel
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SetupViewModel


//settings of bluetooth that will make it possible to scan and pair devices
@Composable
fun SlaveBluetoothScreen(
    navController: NavController,
    setupViewModel: SetupViewModel,
    channelViewModel: ChannelViewModel,
    bluetoothViewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    val state by bluetoothViewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Row() {
            Text(text = "BT Connections")
            Button(
                //navigate to home
                //will do more in the future
                onClick = {
                    navController.navigate("slave_home")
                },
                modifier = Modifier
                    .padding(top = 40.dp, start = 100.dp)

            ) {
                Text(text = "Save")
            }
        }
        when {
            state.isConnecting -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text("Connecting...")
                }
            }
            else -> {
                BluetoothDevicesScreen(
                    setupViewModel = setupViewModel,
                    state = state,
                    onStartScan = bluetoothViewModel::startScan,
                    onStopScan = bluetoothViewModel::stopScan,
                    onStartServer = {},
                    onDeviceClick = bluetoothViewModel::connectToDevice
                )
            }
        }



    }

}

