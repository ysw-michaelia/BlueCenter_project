package com.example.datalogger.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.io.File

class SensorController(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val activeSensorListeners = mutableMapOf<Int, Pair<SensorEventListener, Int>>()

    fun getSensorByType(sensorType: Int): Sensor? {
        return sensorManager.getDefaultSensor(sensorType)
    }

    // Start recording for a sensor, if not already started
    fun startSensor(sensor: Sensor, onDataReceived: (FloatArray) -> Unit) {
        val sensorType = sensor.type
        val (listener, count) = activeSensorListeners[sensorType] ?: createSensorListener(sensor, onDataReceived)

        //if new, register listener
        if (count == 0) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        //update count
        activeSensorListeners[sensorType] = listener to (count + 1)
    }

    // Stop recording for a sensor, only if no other channels are using it
    fun stopSensor(sensorType: Int) {
        val (listener, count) = activeSensorListeners[sensorType] ?: return

        if (count == 1) {
            //last listener, unregister
            sensorManager.unregisterListener(listener)
            activeSensorListeners.remove(sensorType)
        } else {
            //update count
            activeSensorListeners[sensorType] = listener to (count - 1)
        }
    }

    // Helper function to create a new listener for a sensor
    private fun createSensorListener(sensor: Sensor, onDataReceived: (FloatArray) -> Unit): Pair<SensorEventListener, Int> {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // callback
                onDataReceived(event.values)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //will add in the future if needed
            }
        }
        return listener to 0
    }

    fun saveSensorDataToFile(context: Context, data: String) {
        val fileName = "log_data.txt"
        val file = File(context.filesDir, fileName)
        file.appendText("$data\n")
    }
}
