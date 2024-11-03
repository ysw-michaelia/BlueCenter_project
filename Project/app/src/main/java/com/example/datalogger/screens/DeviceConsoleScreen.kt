package com.example.datalogger.screens

import androidx.compose.ui.platform.LocalContext
import android.app.Application
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datalogger.state.BluetoothViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("MissingPermission")
@Composable
fun DeviceConsoleScreen(
    navController: NavController,
    bluetoothViewModel: BluetoothViewModel,
    device: android.bluetooth.BluetoothDevice,
    onSendCommand: (String)-> Unit,
    modifier: Modifier = Modifier
) {
    var context = LocalContext.current
//    val bluetoothViewModel: BluetoothViewModel = viewModel(
//        factory = BluetoothViewModelFactory(context.applicationContext as Application)
//    )
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
                bluetoothViewModel.sendCommand("ciao", device.address)
            },
            modifier = Modifier.padding(top = 40.dp, start = 40.dp)
        ) {
            Text("Send command")

        }
         // Input for sampling rate
         var samplingRate by remember { mutableStateOf("") }
          TextField(
             value = samplingRate,
             onValueChange = { samplingRate = it },
             label = { Text("Sampling Rate (Hz)") },
             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
             modifier = Modifier.fillMaxWidth()
         )

         Spacer(modifier = Modifier.height(8.dp))

         Button(
             onClick = {
                 val rate = samplingRate.toIntOrNull()
                 if (rate != null && rate > 0) {
                     bluetoothViewModel.sendSamplingRateCommand(rate, device.address)
                 } else {
                     Toast.makeText(
                         context,
                         "Please enter a valid positive number.",
                         Toast.LENGTH_SHORT
                     ).show()
                 }
             }
         ) {
             Text("Set Sampling Rate")
         }

         //Spacer(modifier = Modifier.height(16.dp))
         // Add a new button for starting sampling
//         Button(
//             onClick = {
//                 // Send the "start sampling" command to the slave device
//                 bluetoothViewModel.sendCommand("[START_SAMPLING]", device.address)
//             },
//             modifier = Modifier.padding(top = 16.dp)
//         ) {
//             Text("Start Sampling")
//         }

         Spacer(modifier = Modifier.height(16.dp))
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