package com.example.datalogger.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.io.File

class SensorController(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var sensorEventListener: SensorEventListener? = null

    fun getSensorByType(sensorType: Int): Sensor? {
        return sensorManager.getDefaultSensor(sensorType)
    }

    // start recording
    fun startSensor(sensor: Sensor, onDataReceived: (FloatArray) -> Unit) {
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // callback
                onDataReceived(event.values)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // stop recording
    fun stopSensor() {
        sensorEventListener?.let {
            sensorManager.unregisterListener(it)
            sensorEventListener = null
        }
    }

    // get list of available sensors
    fun getAvailableSensors(): List<Sensor> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
    }

    fun saveSensorDataToFile(context: Context, data: String) {
        val fileName = "log_data.txt"
        val file = File(context.filesDir, fileName)
        file.appendText("$data\n")
    }
}
