package com.example.datalogger

import android.app.Application
import com.example.datalogger.data.Graph

//class copied by a tutorial, it is needed for database functionality
class DataLoggerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }

}