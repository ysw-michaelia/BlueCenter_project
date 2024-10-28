package com.example.datalogger.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datalogger.state.BluetoothViewModel

@SuppressLint("MissingPermission")
@Composable
fun DeviceConsoleScreen(
    navController: NavController,
    bluetoothViewModel: BluetoothViewModel,
    device: android.bluetooth.BluetoothDevice,
    modifier: Modifier = Modifier
) {
    Column() {
        Text("This is ${device.name}",
            modifier = Modifier.padding(50.dp))
        Button(
            //reset the settings in the shared preferences and navigate to setup
            //also deletes all channels in the database
            onClick = {
                navController.navigate("master_home")
            },
            modifier = Modifier.padding(top = 40.dp, start = 40.dp)
        ) {
            Text(text = "Go back")
        }
    }
    //insert console here
}