package com.example.datalogger.state

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.datalogger.di.DatabaseModule
import com.example.datalogger.repository.ChannelRepository
import com.example.datalogger.sensor.SensorController
import com.example.datalogger.sensor.SensorLogFileManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

//Future view model that will be used to store the state of the sensors
class SensorViewModel(application: Application): AndroidViewModel(application) {
    private val sensorController = SensorController(application)
    private val sensorLogFileManager = SensorLogFileManager(application)
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

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
    }

    fun resetSamplesNumber() {
        _samples.update { 0 }

    }

    private fun registerAndStartMonitoring(sensorType: Int) {
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

    fun handleSensorData(sensorType: Int, sensor2: Int) {
        Log.d("SensorViewModel", "Current sensor type: $currentSensorType")
        val isSampling = _isSamplingRequested.value
        val sampleLimit = _samples.value

        //Log.d("SensorData", "isSampling: $isSampling, sampleLimit: $sampleLimit")


        sensorController.stopSensor(sensorType)
        sensorController.stopSensor(sensor2)

        sensorController.startSensor(sensorType) { data ->
            Log.d("SensorData", data.joinToString(", "))

            val formattedData = data.joinToString(",") { value ->
                "%.4f".format(value)
            } + "\n"
            //Log.d("SensorData", "Formatted data: $formattedData")
            Log.d("SensorViewModel", "Before if check: $isSampling, ${sensorDataList.size} < $sampleLimit}")
            if (isSampling && sensorDataList.size < sampleLimit) {
                sensorDataList.add(formattedData)
                Log.d("SensorData", "Added to list: $formattedData")
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
                    registerAndStartMonitoring(it.sensorType)
                } else {
                    sensorController.stopSensor(it.sensorType)
                }
            }
        }
    }

    fun timeChecking(channelId: Int) {
        viewModelScope.launch {
            val channel = channelRepository.getChannelById(channelId).firstOrNull()

            if (channel == null) {
                Log.d("SensorLogging", "Channel not found")
                return@launch
            }

            val sensorType = channel.sensorType

            if (sensorType == 0) {
                Log.d("SensorLogging", "Sensor type not found")
                return@launch
            }

            val startTime = channel.startTime.toLocalTime()
            val stopTime = channel.stopTime.toLocalTime()
            var currentTime = LocalTime.now()

            if (currentTime.isAfter(startTime) && currentTime.isBefore(stopTime)) {
                handleSensorLogging(channel.name, sensorType)
            }

            while (currentTime.isBefore(startTime)) {
                currentTime = LocalTime.now()
                delay(10000)
                Log.d("SensorLogging", "Waiting for start time: $startTime")
            }

            Log.d("SensorLogging", "Start time reached: $startTime")
            handleSensorLogging(channel.name, sensorType)

            while (currentTime.isBefore(stopTime)) {
                currentTime = LocalTime.now()
                delay(10000)
                Log.d("SensorLogging", "Local time: $currentTime Waiting for stop time: $stopTime")
            }

            Log.d("SensorLogging", "Stop time reached: $stopTime")
            sensorLogFileManager.stopLogging()
            sensorLogFileManager.saveFile()

            //unregister failed, check later
            sensorController.stopSensor(channel.sensorType)
        }
    }

    private fun handleSensorLogging(channelName: String, sensorType: Int) {
        sensorLogFileManager.createFile(channelName)
        Log.d("SensorLogging", "Channel Name: $channelName")

        if (sensorType != 0) {
            sensorController.startSensor(sensorType) { data ->
                Log.d("SensorData", "Sensor data: ${data.joinToString(", ")}")
                sensorLogFileManager.startLogging(data.joinToString(", "))
            }
        } else {
            Log.d("SensorLogging", "Invalid sensor type: $sensorType")
        }
    }

    fun triggerLevelChecking(channelId: Int, triggerLevel: Float) {
        viewModelScope.launch {
            val channel = channelRepository.getChannelById(channelId).firstOrNull()
            if (channel == null) {
                Log.d("SensorLogging", "Channel not found")
                return@launch
            }

            val sensorType = channel.sensorType
            if (sensorType == 0) {
                Log.d("SensorLogging", "Sensor type not found")
                return@launch
            }

            var startTime: Long = 0
            var isLoggingStarted = false
            var isLoggingStopped = false

            sensorController.startSensor(sensorType) { data ->
                Log.d("SensorData", "Sensor data: ${data.joinToString(", ")}")

                if (data.first() >= triggerLevel) {
                    if (startTime == 0L) {
                        startTime = System.currentTimeMillis()
                    }

                    if (!isLoggingStarted && System.currentTimeMillis() - startTime >= 5000) {
                        sensorLogFileManager.createFile(channel.name)
                        sensorLogFileManager.startLogging(data.joinToString(", "))
                        isLoggingStarted = true
                        Log.d("SensorLogging", "Logging started")
                    }

                } else {
                    if (isLoggingStarted && System.currentTimeMillis() - startTime >= 5000) {
                        sensorLogFileManager.stopLogging()
                        sensorLogFileManager.saveFile()
                        isLoggingStopped = true
                        Log.d("SensorLogging", "Logging stopped and saved")
                    }
                }

                if (isLoggingStarted && isLoggingStopped) {
                    startTime = System.currentTimeMillis()  // 重置开始时间
                    isLoggingStarted = false
                    isLoggingStopped = false
                    Log.d("SensorLogging", "Sensor stopped")
                    sensorController.stopSensor(sensorType)
                }
            }
        }
    }

    fun stopAllSensors() {
        sensorController.stopAllSensors()
    }

    private fun String.toLocalTime(): LocalTime {
        return LocalTime.parse(this, timeFormatter)
    }
}
