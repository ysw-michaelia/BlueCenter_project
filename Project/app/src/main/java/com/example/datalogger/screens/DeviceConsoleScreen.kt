package com.example.datalogger.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.example.datalogger.network.CommandInterpreter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    var command by remember { mutableStateOf("") }
    val context = LocalContext.current

    var previousDeviceCount by remember { mutableStateOf(state.connectedDevices.size) }

    LaunchedEffect(state.connectedDevices.size) {
        if (state.connectedDevices.size < previousDeviceCount) {
            Toast.makeText(context, "A device disconnected", Toast.LENGTH_SHORT).show()
        }
        previousDeviceCount = state.connectedDevices.size
    }

    Column() {
        Row() {
            Text(
                "Console to: ${device.name}",
                modifier = Modifier.padding(50.dp)
            )
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
        TextField(
            value = command,
            onValueChange = { command = it },
            label = { Text("Enter Command") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSendCommand(command) // Process the command
            },
            enabled = !(state.isTalking[device.address] ?: false),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Send Command")
        }
        InteractionLogDisplay(state.interactionLog)
    }
}

@Composable
fun InteractionLogDisplay(interactionLog: Map<String, List<String>>) {
    LazyColumn {
        interactionLog.forEach { (deviceAddress, messages) ->
            // Display the device address as a header

            // Display each message for this device address
            items(messages) { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                )
            }
        }
    }
}
