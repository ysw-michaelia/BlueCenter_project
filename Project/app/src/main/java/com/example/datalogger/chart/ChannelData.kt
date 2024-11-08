package com.example.datalogger.chart

data class ChannelData(
    val xValues: MutableList<Float>,
    val yValues: MutableList<Float>,
    val zValues: MutableList<Float>
)
