package com.example.datalogger.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datalogger.state.BluetoothViewModel
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SetupViewModel

//settings of bluetooth that will make it possible to scan and pair devices
@Composable
fun MasterBluetoothScreen(
    navController: NavController,
    setupViewModel: SetupViewModel,
    channelViewModel: ChannelViewModel,
    bluetoothViewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        val state by bluetoothViewModel.state.collectAsState()
        Row() {
            Text(text = "BT Connections")
            Button(
                //navigate back to master home
                //will do more in future
                onClick = {
                    navController.navigate("master_home")
                },
                modifier = Modifier
                    .padding(top = 40.dp, start = 100.dp)

            ) {
                Text(text = "Save")
            }
        }
        BluetoothDevicesScreen(
            state = state,
            onStartScan = bluetoothViewModel::startScan,
            onStopScan = bluetoothViewModel::stopScan
        )

    }

}