package com.example.datalogger.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.datalogger.data.console.BluetoothCommand

//interface for bluetooth controller that will be implemented by AndroidBluetoothController
interface BluetoothController {

    val isConnected: StateFlow<Boolean>
    val scanDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val connectedDevices: StateFlow<List<BluetoothDevice>>
    val errors: SharedFlow<String?>

    fun startDiscovery()
    fun stopDiscovery()

    fun getDeviceByAddress(address: String): android.bluetooth.BluetoothDevice?
    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    suspend fun trySendCommand(command: String, deviceAddress: String): BluetoothCommand?
    suspend fun trySendFile(fileData: ByteArray, deviceAddress: String): String?
    suspend fun trySendStringReply(message: String, deviceAddress: String): String?

    fun closeConnection()
    fun release()

}