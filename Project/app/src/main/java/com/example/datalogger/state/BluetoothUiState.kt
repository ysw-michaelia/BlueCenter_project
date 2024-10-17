package com.example.datalogger.state

import com.example.datalogger.network.BluetoothDevice


//state class for bluetooth viewmodel
data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList()
)
