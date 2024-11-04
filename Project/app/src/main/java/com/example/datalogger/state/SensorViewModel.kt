package com.example.datalogger.state

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.datalogger.di.DatabaseModule
import com.example.datalogger.repository.ChannelRepository
import com.example.datalogger.sensor.SensorController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

//Future view model that will be used to store the state of the sensors
class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorController = SensorController(application)
    private val channelRepository: ChannelRepository = DatabaseModule.repository
    private val sensorDataList = mutableListOf<String>()  //save sampling data
    private var sampleCount: Int = 0
    var isSamplingRequested = false

    suspend fun isAnyChannelMonitoring(): Boolean {
        val channels = channelRepository.allChannels().first()
        return channels.any { it.isActivated }
    }

    fun startSampling(samples: Int) {
        sampleCount = samples
        isSamplingRequested = true
    }

    fun getAndClearSampledData(): String {
        val data = sensorDataList.joinToString("\n")
        sensorDataList.clear()
        isSamplingRequested = false
        sampleCount = 0
        return data
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
                    // Directly pass the sensor information from the channel to start monitoring
                    val sensor = sensorController.getSensorByType(it.sensorType)
                    if (sensor != null) {
                        sensorController.startSensor(sensor) { data ->
                            //handle the sensor data here
                            Log.d("SensorData", data.joinToString(", "))

                            if (isSamplingRequested && sensorDataList.size < sampleCount) {
                                sensorDataList.add(data.joinToString(", "))
                            }
                        }
                    } else {
                        Log.e("SensorError", "can't find sensor with type ${it.sensorType}")
                    }
                } else {
                    sensorController.stopSensor(it.sensorType)
                }
            }
        }
    }
}