package com.example.datalogger.network

import android.hardware.Sensor
import android.util.Log
import com.example.datalogger.sensor.SensorController

class CommandHandler(private val sensorController: SensorController) {

      fun handleSetSamplingRateCommand(command: String): String {
        // Parse the sampling frequency
        val regex = """\[a,(\d+)]""".toRegex()
        val matchResult = regex.find(command)
        val frequency = matchResult?.groupValues?.get(1)?.toIntOrNull()
        //val sensorType = Sensor.TYPE_ACCELEROMETER

        if (matchResult == null) {
            Log.e("BluetoothService", "Command format is incorrect: $command")
            return "Invalid command format\nERROR"
        }

          if (frequency == null || frequency <= 0) {
              Log.e("CommandHandler", "Invalid frequency: $frequency")
              return "Invalid frequency\nERROR"
          }

          // Set the sampling frequency for all active sensors
          sensorController.setSamplingFrequency(frequency)
          Log.d("SensorController", "Set sampling rate to $frequency Hz for all sensors")

          return "FREQ=$frequency\nOK"
     }

    // Add methods for other commands
}
