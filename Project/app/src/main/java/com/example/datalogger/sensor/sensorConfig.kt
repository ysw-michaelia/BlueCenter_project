package com.example.datalogger.sensor
import android.util.Log

class SensorConfig {
    private var sampleCount: Int = Int.MAX_VALUE // Default to maximum if not set

    fun setSampleCount(count: Int) {
        sampleCount = if (count > 0) count else Int.MAX_VALUE
        Log.d("SensorConfig", "Sample count set to $sampleCount")
    }

    fun getSampleCount(): Int {
        Log.d("SensorConfig", "Getting sample count: $sampleCount")
        return sampleCount
    }
}