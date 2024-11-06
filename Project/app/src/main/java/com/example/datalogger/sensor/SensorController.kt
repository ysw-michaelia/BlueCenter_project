package com.example.datalogger.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SensorController(private val context: Context, private val sensorConfig: SensorConfig) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val activeSensorListeners = mutableMapOf<Int, Pair<SensorEventListener, Int>>()
    private var sampleCounter: Int = 0

    fun getSensorByType(sensorType: Int): Sensor? {
        return sensorManager.getDefaultSensor(sensorType)
    }

    // Start recording for a sensor, if not already started
    fun startSensor(sensor: Sensor, onDataReceived: (FloatArray) -> Unit) {
        val sensorType = sensor.type
        val (listener, count) = activeSensorListeners[sensorType] ?: createSensorListener(sensor, onDataReceived)

        //reset counter
        sampleCounter = 0

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
            Log.d("SensorController", "Sensor type $sensorType stopped and listener removed.")
        } else {
            //update count
            activeSensorListeners[sensorType] = listener to (count - 1)
        }
    }

    // Helper function to create a new listener for a sensor
    private fun createSensorListener(sensor: Sensor, onDataReceived: (FloatArray) -> Unit): Pair<SensorEventListener, Int> {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Check sample count limit before processing
                if (sampleCounter < sensorConfig.getSampleCount()) {
                    onDataReceived(event.values)
                    sampleCounter++
                    Log.d("SensorController", "Sample #$sampleCounter received: ${event.values.joinToString()}")
                } else {
                    // Stop sensor once the sample count is reached
                    stopSensor(sensor.type)
                    Log.d("SensorController", "Target sample count reached, stopping sensor.")
                    sampleCounter = 0 // Reset the counter
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //will add in the future if needed
            }
        }
        return listener to 0
    }
}
