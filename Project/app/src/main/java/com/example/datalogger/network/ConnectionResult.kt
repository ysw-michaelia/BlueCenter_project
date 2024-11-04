package com.example.datalogger.network

sealed interface ConnectionResult {
    data class ConnectionEstablished(val deviceAddress: String): ConnectionResult
    data class StringReceived(val message: String, val deviceAddress: String): ConnectionResult
    data class FileReceived(val fileData: ByteArray, val deviceAddress: String): ConnectionResult
    data class Error(val message: String): ConnectionResult
}