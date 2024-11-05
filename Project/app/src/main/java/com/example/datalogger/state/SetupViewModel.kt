package com.example.datalogger.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.datalogger.data.Channel
import com.example.datalogger.data.PreferencesManager

//class that holds the state for setup values, currently has only number of channels
//and bool for isSetupComplete, but will change accordingly
class SetupViewModel(application: Application): AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    //number of channels
    private var _totChannels = MutableLiveData<Int>().apply {
        value = preferencesManager.getNumberOfChannels()
    }
    val totChannels: LiveData<Int> = _totChannels

    private val _isSetupCompleted = MutableLiveData<Boolean>().apply {
        value = preferencesManager.isSetupComplete()
    }
    val isSetupCompleted: LiveData<Boolean> get() = _isSetupCompleted

    private val _isMaster = MutableLiveData<Boolean>().apply {
        value = preferencesManager.isMaster()
    }
    val isMaster: LiveData<Boolean> get() = _isMaster

    fun setMaster(isMaster: Boolean) {
        _isMaster.value = isMaster
        preferencesManager.setMaster(isMaster)
    }

    //function to set the number of channels, should also handle error input
    //but does not work as expected (copied from chatGPT)
    fun setNumberOfChannels(channels: String) {
        val channelCount = channels.toIntOrNull()

        if (channelCount == null) {
            return
        } else {
            _totChannels.value = channelCount
            preferencesManager.setNumberOfChannels(channelCount) // Save to PreferencesManager
        }
    }
    //function to set the setup as completed both in the shared
    // preferences and in the viewmodel variable
    fun setSetupCompleted(completed: Boolean) {
        _isSetupCompleted.value = completed
        preferencesManager.setSetupComplete(completed)
    }

    fun clearPreferences() {
        preferencesManager.clearPreferences()
    }

}