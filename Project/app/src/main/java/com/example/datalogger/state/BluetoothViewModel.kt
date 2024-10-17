package com.example.datalogger.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.datalogger.network.AndroidBluetoothController
import com.example.datalogger.network.BluetoothController
import com.example.datalogger.network.BluetoothDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

//bluetooth viewmodel, similar dependency handling as setup viewmodel
class BluetoothViewModel (application: Application): AndroidViewModel(application) {
    //controller needed for the view model, it's a singleton
    private val bluetoothController: BluetoothController = AndroidBluetoothController(application)

    //state that holds the bluetooth devices, both paired and scanned
    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine (
        bluetoothController.scanDevices,
        bluetoothController.pairedDevices,
        _state
    ) {
        scanDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scanDevices,
            pairedDevices = pairedDevices
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan() {
        bluetoothController.startDiscovery()
    }
    fun stopScan() {
        bluetoothController.stopDiscovery()
    }
}



