package com.example.datalogger.state

import com.example.datalogger.data.console.BluetoothCommand
import com.example.datalogger.network.BluetoothDevice


//state class for bluetooth viewmodel
data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val connectedDevices: List<BluetoothDevice> = emptyList(),
    val interactionLog: Map<String, List<String>> = emptyMap(),
    val receivedMessages: Map<String, List<String>> = emptyMap(),
    val isServerOpen: Boolean = false,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isTalking: Map<String, Boolean> = emptyMap(),
    val errorMessage: String? = null,
)
