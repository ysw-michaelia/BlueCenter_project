package com.example.datalogger.network

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.datalogger.sensor.SensorController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket,
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

        responses.forEach { response ->
            sendString(response.toByteArray())
        }
        // Send the response back to the master
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

    fun listenForIncomingFile(): Flow<ByteArray> = flow {
        if (!socket.isConnected) return@flow
        val buffer = ByteArray(4096) // Larger buffer for file chunks

        while (true) {
            val byteCount = try {
                socket.inputStream.read(buffer)
            } catch (e: IOException) {
                disconnectionCallback(socket)
                throw IOException("Error reading file: ${e.message}")
            }
            emit(buffer.copyOfRange(0, byteCount)) // Emit only the bytes read
        }
    }.flowOn(Dispatchers.IO)

    suspend fun sendFile(fileData: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(fileData)
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

}