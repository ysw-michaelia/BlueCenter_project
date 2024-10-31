package com.example.datalogger.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datalogger.network.BluetoothDevice
import com.example.datalogger.network.BluetoothDeviceDomain
import com.example.datalogger.state.BluetoothUiState
import com.example.datalogger.state.SetupViewModel

//composable that will show all scanned and paired devices on screen
@Composable
fun BluetoothDevicesScreen(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    disconnectFromDevice: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit,

) {
    val context = LocalContext.current
    var wasConnected by remember { mutableStateOf(state.isConnected) }

// Check if the connection state changed from connected to disconnected
    LaunchedEffect(state.isConnected) {
        if (wasConnected && !state.isConnected) {
            Toast.makeText(context, "You're disconnected", Toast.LENGTH_SHORT).show()
        }
        wasConnected = state.isConnected // Update the last known connection state
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        BluetoothDeviceList(
            pairedDevices = state.pairedDevices,
            scannedDevices = state.scannedDevices,
            onClick = onDeviceClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        val messages = state.receivedCommands ?: emptyList()
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
        //this row holds two buttons that can either start or stop the scan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onStartScan) {
                Text(text = "Start scan")
            }
            Button(onClick = onStopScan) {
                Text(text = "Stop scan")
            }
            val isEnabled = state.isConnected
            Button(
                onClick = disconnectFromDevice,
                enabled = isEnabled
            ) {
                Text(text = "Disconnect")
            }
        }
    }
}

//composable that shows the list of paired and scanned devices
@Composable
fun BluetoothDeviceList(
    pairedDevices: List<BluetoothDevice>,
    scannedDevices: List<BluetoothDevice>,
    onClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            Text(
                text = "Paired Devices",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
        items(pairedDevices) { device->
            Text(
                text = device.name ?: "(No name)",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(device) }
                    .padding(16.dp)
            )
        }
        item {
            Text(
                text = "Scanned Devices",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(scannedDevices) { device->
            Text(
                text = device.name ?: "(No name)",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(device) }
                    .padding(16.dp)
            )
        }
    }
}