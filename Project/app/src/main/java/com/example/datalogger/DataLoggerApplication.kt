package com.example.datalogger

import android.app.Application
import com.example.datalogger.di.DatabaseModule

//class copied by a tutorial, it is needed for database functionality
class DataLoggerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseModule.provide(this)
    }
}