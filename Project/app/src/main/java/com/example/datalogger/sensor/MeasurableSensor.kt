package com.example.datalogger.sensor

//classes created from a video tutorial, still working on their functionality
abstract class MeasurableSensor(
    protected val sensorType: Int
) {

    protected var onSensorValuesChanged: ((List<Float>) -> Unit)? = null

    abstract val doesSensorExist: Boolean

    abstract fun startListening()
    abstract fun stopListening()

    fun onSensorValueChangedListener(listener: (List<Float>) -> Unit) {
        onSensorValuesChanged = listener
    }
}