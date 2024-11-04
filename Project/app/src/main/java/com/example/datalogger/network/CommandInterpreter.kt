package com.example.datalogger.network


import com.example.datalogger.state.BluetoothViewModel

class CommandInterpreter(

) {


    fun interpret(command: String): MutableList<String> {
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

    private fun sampleAndTransferData(): MutableList<String> {
        //start sampling here
        // val sampledData = readFromSensor() or whatever
        //sampledData.add("END")
        //return sampling here
        val acaso = mutableListOf<String>()
        acaso.add("START:")
        acaso.add("END")
        return acaso
        //return mutableListOf("Sampling")
    }

    private fun sendSampledData(): MutableList<String> {
        //return "DATA:\n$data\nEND"
        return mutableListOf("DATA")
    }

    private fun setClock(params: String): MutableList<String> {
        val parts = params.split("-")

        val year = parts.getOrNull(0)?.toIntOrNull() ?: return mutableListOf("Error: Invalid year")
        val month = parts.getOrNull(1)?.toIntOrNull() ?: return mutableListOf("Error: Invalid month")
        val day = parts.getOrNull(2)?.toIntOrNull() ?: return mutableListOf("Error: Invalid day")
        val hour = parts.getOrNull(3)?.toIntOrNull() ?: return mutableListOf("Error: Invalid hour")
        val minute = parts.getOrNull(4)?.toIntOrNull() ?: return mutableListOf("Error: Invalid minute")
        val second = parts.getOrNull(5)?.toIntOrNull() ?: return mutableListOf("Error: Invalid second")

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
        val channels = params.split(":").map { it.toInt() }
        return mutableListOf("Active channels set\nOK")
    }

    private fun showMode(): MutableList<String> {
        return mutableListOf("showing")
    }

    private fun setStreamingRate(params: String): MutableList<String> {
        val rate = params.toIntOrNull() ?: return mutableListOf("Error: Invalid streaming rate")

        //return "Streaming rate set to $rate Hz\nOK"
        return mutableListOf("rate")
    }

    private fun stopSampling(): MutableList<String>{

        return mutableListOf("Sampling stopped\nOK")
    }

    private fun showVersion(): MutableList<String> {
        return mutableListOf("DataLogger App Version: 1.0.0\nOK")
    }
}