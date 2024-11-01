package com.example.datalogger.screens

import android.annotation.SuppressLint
<<<<<<< Updated upstream
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
=======
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
>>>>>>> Stashed changes
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datalogger.network.CommandInterpreter
import com.example.datalogger.state.BluetoothViewModel

@SuppressLint("MissingPermission")
@Composable
fun DeviceConsoleScreen(
    navController: NavController,
    bluetoothViewModel: BluetoothViewModel,
    device: android.bluetooth.BluetoothDevice,
<<<<<<< Updated upstream
    modifier: Modifier = Modifier
) {
    Column() {
        Text("This is ${device.name}",
            modifier = Modifier.padding(50.dp))
=======
    onSendCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val commandInterpreter = CommandInterpreter()
    var command by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Connected to ${device.name}", modifier = Modifier.padding(16.dp))

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
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Send Command")
        }

        Text(
            text = "Response: $response",
            modifier = Modifier.padding(top = 16.dp)
        )

>>>>>>> Stashed changes
        Button(
            onClick = {
                navController.navigate("master_home")
            },
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(text = "Go back")
        }
    }
<<<<<<< Updated upstream
    //insert console here
}
=======
}
>>>>>>> Stashed changes
