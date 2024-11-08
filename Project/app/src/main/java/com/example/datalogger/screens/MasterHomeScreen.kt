package com.example.datalogger.screens

import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datalogger.data.Channel
import com.example.datalogger.network.BluetoothDevice
import com.example.datalogger.state.BluetoothViewModel
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SetupViewModel

//home screen for master
@Composable
fun MasterHomeScreen(
    navController: NavController,
    setupViewModel: SetupViewModel,
    channelViewModel: ChannelViewModel,
    bluetoothViewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    val state by bluetoothViewModel.state.collectAsState()
    val context = LocalContext.current
    var previousDeviceCount by remember { mutableStateOf(state.connectedDevices.size) }

    LaunchedEffect(state.connectedDevices.size) {
        if (state.connectedDevices.size < previousDeviceCount) {
            Toast.makeText(context, "A device disconnected", Toast.LENGTH_SHORT).show()
        }
        previousDeviceCount = state.connectedDevices.size
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = modifier.padding(40.dp),
                text = "HOME",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                //reset the settings in the shared preferences and navigate to setup
                //also deletes all channels in the database
                onClick = {
                    setupViewModel.clearPreferences()
                    channelViewModel.deleteChannels()
                    bluetoothViewModel.disconnectFromDevice()
                    navController.navigate("setup")
                },
                modifier = Modifier.padding(top = 40.dp, start = 40.dp)
            ) {
                Text(text = "Reset")
            }
        }
        val isServerOpen = state.isServerOpen
        if(state.connectedDevices.isNotEmpty()) {
            LazyColumn(
            ) {
                items(state.connectedDevices) { device ->
                    DeviceCard(
                        device = device,
                        onLongPress = { selectedDevice ->
                            //if any channel is long pressed, navigate to channel settings
                            navController.navigate("device_console/${selectedDevice.address}")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        else {
            Text(text = "No connected devices")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(onClick = bluetoothViewModel::waitForIncomingConnections,
                enabled = !isServerOpen) {
                Text(text = "Start server")

            }
            //stop server button
            Button(onClick = bluetoothViewModel::disconnectFromDevice,
                enabled = isServerOpen)
            {
                Text(text = "Stop server")
            }
        }
    }

}

@Composable
fun DeviceCard(
    device: BluetoothDevice,
    onLongPress: (BluetoothDevice) -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress(device) //whenever longpressed, goes back and returns channel
                    }
                )
            }
    ) {
        Text(
            text = device.name ?: "(No name)",
            fontSize = 20.sp
        )
    }
}

