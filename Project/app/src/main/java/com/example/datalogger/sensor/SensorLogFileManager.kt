package com.example.datalogger.sensor

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorLogFileManager(private val context: Context) {
    private var file: File? = null
    private var fileOutputStream: FileOutputStream? = null
    private var fileIndex = 1
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.getDefault())
    var isRecording = false

    //create file with unique name and file header
    fun createFile(channel: String) {
        val fileName = "datalog_$channel.txt"
        file = File(context.filesDir, fileName)
        fileOutputStream = FileOutputStream(file, true)

        val header = "Date/Time\t$channel\n"
        fileOutputStream?.write(header.toByteArray())

        fileIndex++
        isRecording = true
    }

    //start recording data
    fun startLogging(sensorData: Any) {
        if (isRecording) {
            fileOutputStream?.let {
                val timestamp = dateFormat.format(Date())
                val dataRow = "$timestamp\t$sensorData\n"
                it.write(dataRow.toByteArray())
                it.flush()
            }
        }
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