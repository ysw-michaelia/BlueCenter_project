package com.example.datalogger.data.console

fun BluetoothCommand.toByteArray(): ByteArray {
    return command.toByteArray()
}