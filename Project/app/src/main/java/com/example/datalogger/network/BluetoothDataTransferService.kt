package com.example.datalogger.network

import android.bluetooth.BluetoothSocket
import android.hardware.Sensor
import android.util.Log
import com.example.datalogger.sensor.SensorController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket, context: android.content.Context,

) {
    private val sensorController = SensorController(context)
    private val commandHandler = CommandHandler(sensorController)

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
                Log.e("BluetoothDataTransferService", "Socket is not connected")
                return@flow
            }
            val buffer = ByteArray(1024)

            while (true) {

                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    Log.e("BluetoothDataTransferService", "Error reading command: ${e.message}")
                    emit("Error reading command: ${e.message}")
                }
                if (byteCount == -1) {
                    Log.e("BluetoothDataTransferService", "Disconnected from socket")
                    break // Exit the loop if disconnected
                }
                val receivedCommand = buffer.copyOfRange(0, byteCount as Int).decodeToString()
                Log.d("BluetoothDataTransferService", "Received command: $receivedCommand")

                emit(receivedCommand)

                // Automatically reply based on the command received
                handleCommand(receivedCommand)
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun handleCommand(command: String) {
        Log.d("BluetoothService", "Received command: $command")
        val response = when {
            command.startsWith("[a,") -> {
                commandHandler.handleSetSamplingRateCommand(command)
            }
//            command == "[START_SAMPLING]" -> {
//                startSampling()
//                "Sampling started.\nOK"
//            }
            command == "ciao" -> "Potato"
            else -> "Unknown command"
        }

        // Send the response back to the master
        sendString(response.toByteArray())
        Log.d("BluetoothService", "Sent response: $response")
    }

//    private fun startSampling() {
//        if (!sensorsStarted) {
//            startSensors()
//            sensorsStarted = true
//        }
//    }


    suspend fun sendString(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
                Log.d("BluetoothDataTransferService", "Sent string: ${bytes.decodeToString()}")

            } catch (e: IOException) {
                Log.e("BluetoothDataTransferService", "Error sending string: ${e.message}")
                e.printStackTrace()
                return@withContext false
            }
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

//    fun startSensors() {
//        val accelerometer = sensorController?.getSensorByType(Sensor.TYPE_ACCELEROMETER)
//        if (accelerometer != null) {
//            sensorController.startSensor(accelerometer) { data ->
//                Log.d("SensorData", "Accelerometer data: ${data.contentToString()}")
//            }
//        } else {
//            Log.e("BluetoothDataTransferService", "Accelerometer sensor not available")
//        }
//    }
}