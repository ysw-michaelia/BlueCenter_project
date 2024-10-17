package com.example.datalogger.network

import kotlinx.coroutines.flow.StateFlow

//interface for bluetooth controller that will be implemented by AndroidBluetoothController
interface BluetoothController {

    val scanDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>

    fun startDiscovery()
    fun stopDiscovery()

    fun release()
}