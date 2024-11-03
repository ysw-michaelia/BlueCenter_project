package com.example.datalogger.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SensorController(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val activeSensorListeners = mutableMapOf<Int, Pair<SensorEventListener, Int>>()
    // Variable to hold the sampling rate in microseconds
    private var samplingRateInMicroseconds = SensorManager.SENSOR_DELAY_NORMAL
    fun getSensorByType(sensorType: Int): Sensor? {
        return sensorManager.getDefaultSensor(sensorType)
    }

    // Start recording for a sensor, if not already started
    fun startSensor(sensor: Sensor, onDataReceived: (FloatArray) -> Unit) {
        val sensorType = sensor.type
        val (listener, count) = activeSensorListeners[sensorType] ?: createSensorListener(sensor, onDataReceived)

        //if new, register listener
        if (count == 0) {
            val success = sensorManager.registerListener(listener, sensor, samplingRateInMicroseconds)
            Log.d("SensorController", "Listener registered: $success")
        }
        Log.d(
            "SensorController",
            "Started sensor $sensorType with sampling rate $samplingRateInMicroseconds μs"
        )

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
            Log.d("SensorController", "Stopped sensor $sensorType")
        } else {
            //update count
            activeSensorListeners[sensorType] = listener to (count - 1)
        }
    }

    // Helper function to create a new listener for a sensor
    private fun createSensorListener(sensor: Sensor, onDataReceived: (FloatArray) -> Unit): Pair<SensorEventListener, Int> {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                Log.d("SensorController", "onSensorChanged called with values: ${event.values.contentToString()}")
                // callback
                onDataReceived(event.values)
                Log.d("SensorData", "Sensor ${sensor.type} data received at ${System.currentTimeMillis()}")
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //will add in the future if needed
            }
        }
        return listener to 0
    }

    // Function to set the sampling rate in Hz
    @Synchronized
    fun setSamplingFrequency(frequencyHz: Int) {
        if (frequencyHz <= 0) {
            throw IllegalArgumentException("Frequency must be greater than zero")
        }
        // Convert frequency in Hz to delay in microseconds
        samplingRateInMicroseconds = 1_000_000 / frequencyHz
        Log.d("SensorController", "Sampling rate set to $frequencyHz Hz")

        // Re-register all active sensors with the new sampling rate
        synchronized(activeSensorListeners) {
            for ((sensorType, pair) in activeSensorListeners) {
                val (listener, _) = pair
                val sensor = sensorManager.getDefaultSensor(sensorType)

                sensorManager.unregisterListener(listener, sensor)
                sensorManager.registerListener(listener, sensor, samplingRateInMicroseconds)
                Log.d("SensorController", "Re-registered sensor $sensorType with new sampling rate")
            }

        }
    }
    // Function to check if the sampling rate is supported
    fun isSamplingRateSupported(sensorType: Int, frequencyHz: Int): Boolean {
        // Get the sensor's min and max delay
        val sensor = sensorManager.getDefaultSensor(sensorType) ?: return false
        val minDelay = sensor.minDelay // in microseconds
        val maxDelay = sensor.maxDelay // in microseconds

        // Convert frequency in Hz to delay in microseconds
        val requestedDelay = 1_000_000 / frequencyHz
        Log.d("SensorController", "Requested frequency: $frequencyHz Hz, requested delay: $requestedDelay μs, minDelay: $minDelay μs, maxDelay: $maxDelay μs")

        // Check if the requested delay is within the supported range
        return (minDelay == 0 || requestedDelay >= minDelay) &&
                (maxDelay == 0 || requestedDelay <= maxDelay)
    }

//    fun setSampleCount(count: Int) {
//        sampleCount = if (count > 0) count else Int.MAX_VALUE // Use max value as default
//        Log.d("SensorController", "Sample count set to $sampleCount")
//    }
//
//
//    fun getSampleCount(): Int {
//        return sampleCount
//    }
}
