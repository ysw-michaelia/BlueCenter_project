package com.example.datalogger.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datalogger.data.Channel
import com.example.datalogger.di.DatabaseModule
import com.example.datalogger.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

//view model that holds the state of the channels
class ChannelViewModel(
    private val repository: ChannelRepository = DatabaseModule.repository
): ViewModel() {

    //variable for number of channels in the database
    private var _channelCount = MutableLiveData<Int>()
    val channelCount: LiveData<Int> = _channelCount

    //variable that holds the list in the database
    private val _channelList = MutableLiveData<List<Channel>>()
    val channelList: LiveData<List<Channel>> = _channelList

    //initialize the class to avoid null values
    init {
        viewModelScope.launch {
            repository.channelCount().collect { count ->
                _channelCount.value = count
            }
        }
        viewModelScope.launch {
            repository.allChannels().collectLatest {
                _channelList.value = it
            }
        }
    }

    //function to collect list from database
    fun getChannelList() {
        viewModelScope.launch {
            repository.allChannels().collectLatest {
                _channelList.value = it
            }
        }
    }

    //function to create list when needed
    fun createChannelList(numberOfChannels: Int) {
        //list created, each channel has default value except the name being "Channel {number}"
        //for default, while the ID will be autogenerated when inserted in the database
        val channels = (1..numberOfChannels).map {
            Channel(name = "Channel $it")
        }
        //insert each channel in the created list
        channels.forEach { channel ->
            viewModelScope.launch {
                repository.upsertChannel(channel)
            }
        }
    }

    //function that returns a channel given the ID
    fun getChannelById(channelId: Int): Flow<Channel> {
        return repository.getChannelById(channelId)
    }

    //function that upserts a channel and refreshes the values of the viewmodel
    fun upsertChannel(channel: Channel) {
        viewModelScope.launch {
            repository.upsertChannel(channel)
            _channelCount.value = repository.channelCount().first()
            _channelList.value = repository.allChannels().first()
        }



    }

    fun deleteChannels() {
        viewModelScope.launch {
            repository.deleteAllChannels()
        }
    }
}