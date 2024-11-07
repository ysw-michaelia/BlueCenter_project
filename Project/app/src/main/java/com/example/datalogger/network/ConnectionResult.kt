package com.example.datalogger.network

import java.io.File

sealed interface ConnectionResult {
    data class ConnectionEstablished(val deviceAddress: String): ConnectionResult
    data class StringReceived(val message: String, val deviceAddress: String): ConnectionResult
    data class FileReceived(val file: File, val deviceAddress: String): ConnectionResult
    data class Error(val message: String): ConnectionResult
}