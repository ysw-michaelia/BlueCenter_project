package com.example.datalogger.network


import com.example.datalogger.state.BluetoothViewModel

class CommandInterpreter() {


    fun interpret(command: String): String {
        val commandType = command.take(2)
        val parameters = command.drop(3)

        return when (commandType) {
            "[a" -> setSamplingRate(parameters)
            "[b" -> setNumberOfSamples(parameters)
            "[c" -> setSamplingConfiguration(parameters)
            "[d" -> sampleAndTransferData()
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
            else -> "Error: Unsupported Command"
        }
    }

    private fun setSamplingRate(params: String): String {
        val rate = params.toIntOrNull() ?: return "Error: Invalid rate"
        return "FREQ=$rate\nOK"
    }

    private fun setNumberOfSamples(params: String): String {
        val samples = params.toIntOrNull() ?: return "Error: Invalid sample number"
        return "SAMPLES=$samples\nOK"
    }

    private fun setSamplingConfiguration(params: String): String {
        val (dataFormat, transmitAfterSampling, externalTrigger, timestamp) = params.split(":")
        return "OK"
    }

    private fun sampleAndTransferData(): String {
        //start sampling here
        
        //return sampling here

        return "SAMPLING"
    }

    private fun sendSampledData(): String {
        //return "DATA:\n$data\nEND"
        return "DATA"
    }

    private fun setClock(params: String): String {
        val parts = params.split("-")

        val year = parts.getOrNull(0)?.toIntOrNull() ?: return "Error: Invalid year"
        val month = parts.getOrNull(1)?.toIntOrNull() ?: return "Error: Invalid month"
        val day = parts.getOrNull(2)?.toIntOrNull() ?: return "Error: Invalid day"
        val hour = parts.getOrNull(3)?.toIntOrNull() ?: return "Error: Invalid hour"
        val minute = parts.getOrNull(4)?.toIntOrNull() ?: return "Error: Invalid minute"
        val second = parts.getOrNull(5)?.toIntOrNull() ?: return "Error: Invalid second"

        return "Clock set: $hour:$minute:$second, Date:$day/$month, Year:$year\nStatus:Ready.\nOK"
    }

    private fun showDeviceStatus(): String {
        //return "Frequency: ${status.frequency}\nSamples: ${status.samples}\nNr of active channels: ${status.activeChannels}\nStatus: ${status.status}\nOK"
        return "status"
    }

    private fun setWaitTime(params: String): String {
        val seconds = params.toIntOrNull() ?: return "Error: Invalid wait time"
        //return "Seconds=$seconds\nOK"
        return "second"
    }

    private fun displayTime(): String {
        //return "Clock set: ${currentTime.time}, Date:${currentTime.date}\nOK"
        return "TIME"
    }

    private fun setActiveChannels(params: String): String {
        val channels = params.split(":").map { it.toInt() }
        return "Active channels set\nOK"
    }

    private fun showMode(): String {
        return "showing"
    }

    private fun setStreamingRate(params: String): String {
        val rate = params.toIntOrNull() ?: return "Error: Invalid streaming rate"

        //return "Streaming rate set to $rate Hz\nOK"
        return "rate"
    }

    private fun stopSampling(): String {

        return "Sampling stopped\nOK"
    }

    private fun showVersion(): String {
        return "DataLogger App Version: 1.0.0\nOK"
    }
}