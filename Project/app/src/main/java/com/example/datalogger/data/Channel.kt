package com.example.datalogger.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//data class used in the database for the channels
@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "channel_id")
    var channelId: Int = 0, //id of each channel, autogenerated to avoid confusion
    @ColumnInfo(name = "channel_name")
    var name: String = "", //name of the channel, can be renamed
    @ColumnInfo(name = "channel_sensor")
    var sensorName: String = "None", //info about sensors for further use
    @ColumnInfo(name = "channel_sensor_type")
    var sensorType: Int = 0, //info about sensors for further use
    @ColumnInfo(name = "is_activated")
    var isActivated: Boolean = false, //track activation status
    @ColumnInfo(name = "is_static")
    var isStatic: Boolean = false, //checks if it is dummy sensor or not
    @ColumnInfo(name = "static_value")
    var staticValue: Float = 0F, //static value, if needs to be referenced
    @ColumnInfo(name = "start_time")
    var startTime: String = "00:00", //start time
    @ColumnInfo(name = "stop_time")
    var stopTime: String = "00:00", //stop time
    @ColumnInfo(name = "has_trigger_level")
    var hasTriggerLevel: Boolean = false, //checks if it has a trigger level
    @ColumnInfo(name = "above_trigger_level")
    var aboveTriggerLevel: Boolean = true, //determines if we get samples above trigger, if false we get below
    @ColumnInfo(name = "trigger_level")
    var triggerLevel: Float = 0F, //trigger level
    )
