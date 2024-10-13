package com.example.datalogger.data

import android.content.Context
import com.example.datalogger.repository.ChannelRepository

//singleton for the database, syntax copied by a video tutorial
object Graph {
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