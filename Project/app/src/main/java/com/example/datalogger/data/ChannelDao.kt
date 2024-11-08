package com.example.datalogger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

//data access object for database
@Dao
interface ChannelDao {

    //will insert in case id doesn't exist yet
    //will update in case of conflict
    @Upsert
    suspend fun upsertChannel(channel: Channel)

    //get the number of channels in the database
    @Query("SELECT COUNT(*) FROM channels")
    fun getChannelCount(): Flow<Int>

    //find a channel by id
    @Query("SELECT * FROM channels WHERE channel_id = :channelId")
    fun getChannelById(channelId: Int): Flow<Channel>

    //get all the channels in the database
    @Query("SELECT * FROM channels WHERE is_activated = 1 AND channel_sensor_type != 0")
    fun getActiveChannelWithSensor(): Flow<List<Channel>>

    //get all the channels in the database
    @Query("SELECT * FROM channels")
    fun getAllChannels(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE is_activated = true")
    fun getActiveChannels(): Flow<List<Channel>>

    //delete all the channels in the database
    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()

    @Query("UPDATE channels SET master_timestamp = :newValue")
    suspend fun updateAllMasterTimestamps(newValue: Boolean)
}
