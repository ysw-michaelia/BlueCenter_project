package com.example.datalogger.network

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket,
    private val context: Context,
    private val commandInterpreter: CommandInterpreter,
    private val disconnectionCallback: (socket: BluetoothSocket) -> Unit
) {
    fun listenForIncomingString(): Flow<String> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)

            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    disconnectionCallback(socket)
                    emit("Error reading input: ${e.message}")
                }
                if (byteCount == -1) {
                    break // Exit the loop if disconnected
                }
                Log.d("ricevuto", "Ricevuto: ${buffer.copyOfRange(0, byteCount as Int).decodeToString()}")

                emit(buffer.copyOfRange(0, byteCount as Int).decodeToString())
            }
        }.flowOn(Dispatchers.IO)
    }

    fun listenForIncomingCommand(): Flow<String> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)

            while (true) {

                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    emit("Error reading command: ${e.message}")
                }
                if (byteCount == -1) {
                    break // Exit the loop if disconnected
                }
                val receivedCommand = buffer.copyOfRange(0, byteCount as Int).decodeToString()
                Log.d("ricevuto", "Ricevuto: ${buffer.copyOfRange(0, byteCount as Int).decodeToString()}")


                emit(receivedCommand)

                handleCommand(receivedCommand)

            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun handleCommand(command: String) {
        val responses = commandInterpreter.interpret(command)

        if (responses.size == 1 && responses[0] == "SEND FILES") {
            val filesDir = context.filesDir
            val files = filesDir.listFiles()
            if (files == null) {
                sendString("No files found".toByteArray())
            }
            files?.forEach { file ->
                if (file.isFile) {
                    sendFile(file)
                    Log.d("Bluetooth", "Sent file: ${file.name}")
                }
            }
        }

        else {
            responses.forEach { response ->
                sendString(response.toByteArray())
            }
        }
    }

    suspend fun sendString(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext false
            }
            Log.d("sent", "Inviato: ${bytes.decodeToString()}")
            true
        }
    }

    fun listenForIncomingFile(): Flow<File> = flow {
        if (!socket.isConnected) return@flow
        val buffer = ByteArray(4096)

        // Generate a unique file name
        val file = getUniqueFileName("Logdata", "txt")

        file.outputStream().use { fileOutputStream ->
            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    disconnectionCallback(socket)
                    throw IOException("Error reading file: ${e.message}")
                }

                if (byteCount == -1) break // End of stream

                // Write to file
                fileOutputStream.write(buffer, 0, byteCount)
            }
        }
        emit(file) // Emit the complete file once finished
    }.flowOn(Dispatchers.IO)


    fun getUniqueFileName(baseName: String, extension: String): File {
        val directory = File(context.filesDir, "") // Adjust directory as needed
        var counter = 1
        var file: File
        do {
            val fileName = baseName +"_$counter.$extension"
            file = File(directory, fileName)
            counter++
        } while (file.exists()) // Increment counter until we find a unique name
        return file
    }

    suspend fun sendFile(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = file.inputStream()
                val fileBytes = inputStream.readBytes()
                socket.outputStream.write(fileBytes)
                inputStream.close()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun sendTimestamp(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis().toString()
                socket.outputStream.write(timestamp.toByteArray())
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun requestTimestamp(): Boolean {
        val bytes = "REQUEST_TIMESTAMP".toByteArray()
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext false
            }
            Log.d("sent", "Inviato: ${bytes.decodeToString()}")
            true
        }
    }
}