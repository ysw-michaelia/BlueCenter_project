package com.example.datalogger.sensor

import android.app.Application
import android.util.Log
import com.example.datalogger.di.DatabaseModule
import com.example.datalogger.repository.ChannelRepository
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.datalogger.state.BluetoothViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SensorLogFileManager(private val application: Application) {
    private val bluetoothViewModel = BluetoothViewModel(application)
    private val channelRepository: ChannelRepository = DatabaseModule.repository
    private var file: File? = null
    private var fileOutputStream: FileOutputStream? = null
    private var fileIndex = 1
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.getDefault())
    private var isRecording = false

    //create file with unique name and file header
    fun createFile(channel: String) {
        val fileName = "datalog_$channel.txt"
        file = File(application.filesDir, fileName)
        fileOutputStream = FileOutputStream(file, true)

        val header = "Date/Time\t$channel\n"
        fileOutputStream?.write(header.toByteArray())

        fileIndex++
        isRecording = true
    }

    //start recording data
    fun startLogging(masterTimestamp: Boolean, sensorData: Any) {
        if (isRecording) {
            fileOutputStream?.let {

                val timestamp = dateFormat.format(Date())
                val dataRow = "$timestamp\t$sensorData\n"
                it.write(dataRow.toByteArray())
                it.flush()
            }
        }
//        if (isRecording) {
//            fileOutputStream?.let { outputStream ->
//
//                // Function to log data with a given timestamp
//                fun logDataWithTimestamp(timestamp: String) {
//                    val dataRow = "$timestamp\t$sensorData\n"
//                    outputStream.write(dataRow.toByteArray())
//                    outputStream.flush()
//                }
//
//                // If we need the master timestamp
//                if (masterTimestamp) {
//                    // Check if the current master address is available
//                    val masterAddress = bluetoothViewModel.state.value.currentMasterAddress
//                    Log.d("SensorLogFileManager", "master address: $masterAddress")
//                    if (masterAddress != null) {
//                        // Request the master timestamp asynchronously
//                        bluetoothViewModel.requestTimestamp(masterAddress)

                        // You should listen for the timestamp reply from the master and log it
//                        bluetoothViewModel.state.onEach { currentState ->
//                            val masterTimestampReceived = currentState. // Assuming this field stores the timestamp
//
//                            // If the master timestamp is received, log the data
//                            if (masterTimestampReceived != null) {
//                                logDataWithTimestamp(masterTimestampReceived)
//                            }
//                        }.launchIn(viewModelScope)

//                    } else {
//                        // If no master address is set, use the current timestamp
//                        val timestamp = dateFormat.format(Date())
//                        logDataWithTimestamp(timestamp)
//                    }
//                } else {
//                    // If masterTimestamp is false, use the current timestamp
//                    val timestamp = dateFormat.format(Date())
//                    logDataWithTimestamp(timestamp)
//                }
//            }
//        }
    }

    //stop recording
    fun stopLogging() {
        isRecording = false
    }

    // save and close
    fun saveFile() {
        fileOutputStream?.close()
        fileOutputStream = null
        isRecording = false
    }
}