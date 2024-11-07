package com.example.datalogger.repository

import com.example.datalogger.data.Channel
import com.example.datalogger.data.ChannelDao


//repository that acts as intermediary for dao and viewmodel
class ChannelRepository(
    private val dao: ChannelDao
) {
    fun channelCount() = dao.getChannelCount()

    fun allChannels() = dao.getAllChannels()

    fun getChannelById(channelId: Int) = dao.getChannelById(channelId)

    fun getActiveChannels() = dao.getActiveChannels()

    fun getActiveChannelWithSensor() = dao.getActiveChannelWithSensor()

    suspend fun upsertChannel(channel: Channel) = dao.upsertChannel(channel)

    suspend fun deleteAllChannels() = dao.deleteAllChannels()
    }
