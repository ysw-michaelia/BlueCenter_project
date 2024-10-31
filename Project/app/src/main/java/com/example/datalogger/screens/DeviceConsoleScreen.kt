package com.example.datalogger.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datalogger.state.BluetoothViewModel

@SuppressLint("MissingPermission")
@Composable
fun DeviceConsoleScreen(
    navController: NavController,
    bluetoothViewModel: BluetoothViewModel,
    device: android.bluetooth.BluetoothDevice,
    onSendCommand: (String)-> Unit,
    modifier: Modifier = Modifier
) {
    val state by bluetoothViewModel.state.collectAsState()
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
        Text("console")
        Button(
            onClick = {
                onSendCommand("ciao")
            },
            modifier = Modifier.padding(top = 40.dp, start = 40.dp)
        ) {
            Text("Send command")

    }
        val messages = state.messages[device.address] ?: emptyList()
        if (messages.isEmpty()) {
            Text("No messages")
        } else {
            // Display the messages
            LazyColumn {
                items(messages) { message ->
                    Text(message)
                }
            }
        }
        //insert console here


    }

}