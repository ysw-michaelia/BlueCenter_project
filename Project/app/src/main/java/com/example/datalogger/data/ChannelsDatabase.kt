package com.example.datalogger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


//actual database class, syntax copied by a video tutorial
@Database(
    entities = [Channel::class],
    version = 1
)
abstract class ChannelsDatabase: RoomDatabase() {

    abstract fun dao(): ChannelDao

    companion object {
        @Volatile
        var INSTANCE : ChannelsDatabase? = null

        //get the database instance
        fun getDatabase(context: Context): ChannelsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    ChannelsDatabase::class.java,
                    name = "channels.db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}