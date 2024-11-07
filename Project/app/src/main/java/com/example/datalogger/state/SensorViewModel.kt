package com.example.datalogger.state

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.datalogger.di.DatabaseModule
import com.example.datalogger.repository.ChannelRepository
import com.example.datalogger.sensor.SensorController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//Future view model that will be used to store the state of the sensors
class SensorViewModel(application: Application): AndroidViewModel(application) {
    private val sensorController = SensorController(application)
    private val channelRepository: ChannelRepository = DatabaseModule.repository
    private val sensorDataList = mutableListOf<String>()  //save sampling data

    private val _currentSensorType = MutableStateFlow<Int?>(null)
    val currentSensorType: StateFlow<Int?>
        get() = _currentSensorType

    private val _samples = MutableStateFlow(0)
    val samples: StateFlow<Int>
        get() = _samples.asStateFlow()

    private val _isSamplingRequested = MutableStateFlow(false)
    val isSamplingRequested: StateFlow<Boolean>
        get() = _isSamplingRequested.asStateFlow()

    suspend fun isAnyChannelMonitoring(): Boolean {
        val channels = channelRepository.allChannels().first()
        return channels.any { it.isActivated }
    }

    fun setSamples(samples: Int) {
        _samples.update { samples }
    }

    fun setSensorType(sensorType: Int) {
        _currentSensorType.update { sensorType }
    }

    fun startSampling() {
        _isSamplingRequested.update { true }
    }

    fun getSampledData(): MutableList<String> {
        return sensorDataList.toMutableList()
    }

    fun clearSampledData() {
        sensorDataList.clear()
        _isSamplingRequested.update { false }
        _samples.update { 0 }
    }

    private fun registerAndStartSampling(sensorType: Int) {
        Log.d("SensorViewModel", "Registering sampling for sensor type: $sensorType")
        val sensor = sensorController.getSensorByType(sensorType)
        if (sensor != null) {
            sensorController.startSensor(sensorType) { data ->
                //handle the sensor data here
                Log.d("SensorData", data.joinToString(", "))}
        } else {
            Log.e("SensorError", "Can't find sensor with type $sensorType")
        }
    }

    fun handleSensorData(sensorType: Int) {
        Log.d("SensorViewModel", "Current sensor type: $currentSensorType")
        val isSampling = _isSamplingRequested.value
        val sampleLimit = _samples.value

        Log.d("SensorData", "isSampling: $isSampling, sampleLimit: $sampleLimit")

        sensorController.stopSensor(sensorType)

        sensorController.startSensor(sensorType) { data ->
            Log.d("SensorData", data.joinToString(", "))

            if (isSampling && sensorDataList.size < sampleLimit) {
                sensorDataList.add(data.joinToString(", "))
                Log.d("SensorData", "Added to list: ${data.joinToString(", ")}")
            }
        }
    }

    //toggle monitoring for a given channel by ID
    fun toggleMonitoring(channelId: Int, isMonitoring: Boolean) {
        viewModelScope.launch {
            //retrieve the channel with the corresponding sensor information
            val channel = channelRepository.getChannelById(channelId).firstOrNull()

            channel?.let {
                //update the isActivated field
                val updatedChannel = it.copy(isActivated = isMonitoring)

                //save the updated channel to the database
                channelRepository.upsertChannel(updatedChannel)

                //start or stop monitoring based on isMonitoring, using the channel's sensor info
                if (isMonitoring) {
                    _currentSensorType.value = it.sensorType
                    Log.d("SensorViewModel", "Sensor type: ${_currentSensorType.value}")
                    registerAndStartSampling(it.sensorType)
                } else {
                    sensorController.stopSensor(it.sensorType)
                }
            }
        }
    }

    fun stopAllSensors() {
        sensorController.stopAllSensors()
    }
}
