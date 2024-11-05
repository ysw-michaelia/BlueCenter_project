package com.example.datalogger.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

//function to convert a bluetooth device to a bluetooth device domain
@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}