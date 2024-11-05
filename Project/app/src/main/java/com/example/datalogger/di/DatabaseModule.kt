package com.example.datalogger.di

import android.content.Context
import com.example.datalogger.data.ChannelsDatabase
import com.example.datalogger.repository.ChannelRepository

//singleton for the database, syntax copied by a video tutorial
object DatabaseModule {
    lateinit var db: ChannelsDatabase
        private set

    val repository by lazy {
        ChannelRepository(
            dao = db.dao()
        )

        }

    fun provide(context: Context) {
        db = ChannelsDatabase.getDatabase(context)

    }
}