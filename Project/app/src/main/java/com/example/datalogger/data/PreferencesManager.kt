package com.example.datalogger.data

import android.content.Context
import android.content.SharedPreferences

//class that manages all preferences
class PreferencesManager(context: Context) {

    //name for shared preferences
    private val SHARED_PREFS_NAME = "Setup_prefs"
    //instance of shared preferences
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    //instance of editor
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    //function to set the boolean value to check if setup is completed
    fun setSetupComplete(isCompleted: Boolean) {
        editor.putBoolean("isSetupCompleted", isCompleted)
        editor.apply()
    }

    //"get" function for isSetupCompleted
    fun isSetupComplete(): Boolean {
        return sharedPreferences.getBoolean("isSetupCompleted", false)
    }

    //reset everything in case of need
    fun clearPreferences() {
        editor.clear()
        editor.apply()
    }
}
