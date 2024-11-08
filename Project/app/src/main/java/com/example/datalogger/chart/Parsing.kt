package com.example.datalogger.chart

import android.util.Log

fun parseChannelData(rawData: List<String>): Map<String, ChannelData> {
    val channelDataMap = mutableMapOf<String, ChannelData>()

    Log.d("parsing", "raw data: $rawData")
    rawData.forEach { string ->
        string.lines().forEach { line ->
            Log.d("parsing", "line: $line")
            // Check for channel name
            var cleanedLine = line
            if (cleanedLine.startsWith(", ")) {
                cleanedLine = cleanedLine.removePrefix(", ")
            }
            Log.d("parsing", "cleaned line: $cleanedLine")
            if (cleanedLine.startsWith("DATA:") || line.startsWith("END")) {
                return@forEach
            }
            if (cleanedLine.startsWith("Channel")) {
                val channelName = cleanedLine.trim()
                channelDataMap[channelName] = ChannelData(mutableListOf(), mutableListOf(), mutableListOf())
            } else if (channelDataMap.isNotEmpty()) {
                // Parse values for the last channel name found
                val values = cleanedLine.split(",")
                if (values.size >= 3) {
                    val (x, y, z) = values.take(3).map {
                        Log.d("Parsing", "Formatted value for $it")
                        it.toFloatOrNull() ?: 0f
                    }
                    Log.d("Parsing", "Parsed values - x: $x, y: $y, z: $z")
                    val currentChannel = channelDataMap.entries.last().value
                    currentChannel.xValues.add(x)
                    currentChannel.yValues.add(y)
                    currentChannel.zValues.add(z)
                }
            }
        }


    }
    return channelDataMap
}
