package com.example.datalogger.network

typealias BluetoothDeviceDomain = BluetoothDevice

//data class for a normal bluetooth device
//MIGHT CHANGE IN THE FUTURE and add more fields
//probably for type of the device (master or slave) to allow multiple connections
//from master to slave but only one from slave to master, while also making it
//not possible to connect to the same device type
data class BluetoothDevice(
    val name: String?,
    val address: String,
)

