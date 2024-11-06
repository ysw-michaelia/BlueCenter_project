package com.example.datalogger.network


import android.util.Log
import com.example.datalogger.di.DatabaseModule.repository
import com.example.datalogger.state.BluetoothViewModel
import com.example.datalogger.state.SensorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class CommandInterpreter(
    private val sensorViewModel: SensorViewModel
) {

    fun interpret(command: String): MutableList<String> {
        val commandType = command.take(2)
        val parameters = command.drop(3)

        return when (commandType) {
            "[a" -> setSamplingRate(parameters)
            "[b" -> setNumberOfSamples(parameters)
            "[c" -> setSamplingConfiguration(parameters)
            // "[d" -> sampleAndTransferData()
            "[e" -> sendSampledData()
            "[f" -> setClock(parameters)
            "[g" -> showDeviceStatus()
            "[h" -> setWaitTime(parameters)
            "[i" -> displayTime()
            "[o" -> setActiveChannels(parameters)
            "[p" -> showMode()
            "[q" -> setStreamingRate(parameters)
            "[s" -> stopSampling()
            "[v" -> showVersion()
            else -> mutableListOf("Error: Unsupported Command")
        }
    }

    private fun setSamplingRate(params: String): MutableList<String> {
        val rate = params.toIntOrNull() ?: return mutableListOf("Error: Invalid rate")
        return mutableListOf("FREQ=$rate\nOK")
    }

    private fun setNumberOfSamples(params: String): MutableList<String> {
        val samples = params.toIntOrNull() ?: return mutableListOf("Error: Invalid sample number")
        return mutableListOf("SAMPLES=$samples\nOK")
    }

    private fun setSamplingConfiguration(params: String): MutableList<String> {
        val (dataFormat, transmitAfterSampling, externalTrigger, timestamp) = params.split(":")
        return mutableListOf("OK")
    }

//    private fun sampleAndTransferData(): MutableList<String> {
//        return runBlocking {
//            if (sensorViewModel.isAnyChannelMonitoring()) {
//                sensorViewModel.startSampling(samples)
//
//                val sampledData = sensorViewModel.getAndClearSampledData()
//                sampledData.add("END")
//                return sampledData
//            } else {
//              return mutableListOf("sampling failed, please start monitoring sensor first.")
//            }
//        }
//    }

    private fun sendSampledData(): MutableList<String> {
        //return "DATA:\n$data\nEND"
        return mutableListOf("DATA")
    }

    private fun setClock(params: String): MutableList<String> {
        val parts = params.split("-")

        val year = parts.getOrNull(0)?.toIntOrNull() ?: return mutableListOf("Error: Invalid year")
        val month =
            parts.getOrNull(1)?.toIntOrNull() ?: return mutableListOf("Error: Invalid month")
        val day = parts.getOrNull(2)?.toIntOrNull() ?: return mutableListOf("Error: Invalid day")
        val hour = parts.getOrNull(3)?.toIntOrNull() ?: return mutableListOf("Error: Invalid hour")
        val minute =
            parts.getOrNull(4)?.toIntOrNull() ?: return mutableListOf("Error: Invalid minute")
        val second =
            parts.getOrNull(5)?.toIntOrNull() ?: return mutableListOf("Error: Invalid second")

        return mutableListOf("Clock set: $hour:$minute:$second, Date:$day/$month, Year:$year\nStatus:Ready.\nOK")
    }

    private fun showDeviceStatus(): MutableList<String> {
        //return "Frequency: ${status.frequency}\nSamples: ${status.samples}\nNr of active channels: ${status.activeChannels}\nStatus: ${status.status}\nOK"
        return mutableListOf("status")
    }

    private fun setWaitTime(params: String): MutableList<String> {
        val seconds = params.toIntOrNull() ?: return mutableListOf("Error: Invalid wait time")
        //return "Seconds=$seconds\nOK"
        return mutableListOf("second")
    }

    private fun displayTime(): MutableList<String> {
        //return "Clock set: ${currentTime.time}, Date:${currentTime.date}\nOK"
        return mutableListOf("TIME")
    }

    private fun setActiveChannels(params: String): MutableList<String> {
        val responseMessages = mutableListOf<String>()
        val channelIds = params.split(":").mapNotNull { it.toIntOrNull() }
        Log.d("CommandInterpreter", "Parsed channel IDs: $channelIds")

        // Using runBlocking to make the function wait for coroutine completion before returning
        runBlocking(Dispatchers.IO) {
            channelIds.forEach { channelId ->
                try {
                    val channel = repository.getChannelById(channelId).firstOrNull()

                    if (channel != null) {
                        channel.isActivated = true
                        repository.upsertChannel(channel) // Update channel status
                        Log.d("CommandInterpreter", "Channel $channelId activated.\nOK")
                        responseMessages.add("Channel $channelId activated.\nOK")
                    } else {
                        Log.d("CommandInterpreter", "Error: Channel $channelId not found.")
                        responseMessages.add("Error: Channel $channelId not found.")
                    }
                } catch (e: Exception) {
                    Log.e("CommandInterpreter", "Exception for channel $channelId: ${e.message}")
                    responseMessages.add("Error: Unable to update channel $channelId. Exception: ${e.message}")
                }
            }
        }

        // If no channels were activated, add a message
        if (responseMessages.isEmpty()) {
            responseMessages.add("No channels were activated.")
        }

        Log.d("CommandInterpreter", "response: ${responseMessages.joinToString()}")
        return responseMessages
    }

    private fun showMode(): MutableList<String> {
        return mutableListOf("showing")
    }

    private fun setStreamingRate(params: String): MutableList<String> {
        val rate = params.toIntOrNull() ?: return mutableListOf("Error: Invalid streaming rate")

        //return "Streaming rate set to $rate Hz\nOK"
        return mutableListOf("rate")
    }

    private fun stopSampling(): MutableList<String> {

        return mutableListOf("Sampling stopped\nOK")
    }

    private fun showVersion(): MutableList<String> {
        return mutableListOf("DataLogger App Version: 1.0.0\nOK")
    }
}
