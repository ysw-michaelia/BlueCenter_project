package com.example.datalogger.state

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.datalogger.data.console.BluetoothCommand
import com.example.datalogger.network.AndroidBluetoothController
import com.example.datalogger.network.BluetoothController
import com.example.datalogger.network.BluetoothDevice
import com.example.datalogger.network.BluetoothDeviceDomain
import com.example.datalogger.network.ConnectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        bluetoothController.connectedDevices,
        _state
    ) {
        scanDevices, pairedDevices, connectedDevices, state ->
        state.copy(
            scannedDevices = scanDevices,
            pairedDevices = pairedDevices,
            connectedDevices = connectedDevices,
            messages = if(state.isServerOpen) state.messages else emptyMap(),
            receivedCommands = if(state.isConnected) state.receivedCommands else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    private var deviceConnectionJob: Job? = null

    init {
        bluetoothController.isConnected.onEach { isConnected->
            _state.update {
                it.copy(
                    isConnected = isConnected
                )
            }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update {
                it.copy(
                    errorMessage = error
                )
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            bluetoothController.connectedDevices.collect { devices ->
                _state.update { it.copy(connectedDevices = devices) }
            }
        }
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        _state.update { it.copy(isConnecting = true) }
        bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update {
            it.copy(
                isConnecting = false,
                isConnected = false,
                isServerOpen = false,
                connectedDevices = emptyList()
            ) }
    }

    fun waitForIncomingConnections() {
        _state.update { it.copy(isServerOpen = true) }

        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    fun sendCommand(command: String, deviceAddress: String) {
        viewModelScope.launch {
            Log.d("BluetoothViewModel", "Attempting to send command: $command to $deviceAddress")
            val bluetoothCommand = bluetoothController.trySendCommand(command, deviceAddress)
            if (bluetoothCommand != null) {
                Log.d("BluetoothViewModel", "Command sent successfully")
            } else {
                Log.e("BluetoothViewModel", "Failed to send command")
            }
//            if (bluetoothCommand != null) {
//                _state.update { it.copy(sentCommands = it.sentCommands + bluetoothCommand) }
//            }
        }
    }

    fun sendStringReply(message: String, deviceAddress: String) {
        viewModelScope.launch {
            val reply = bluetoothController.trySendStringReply(message, deviceAddress)
        }
    }
    fun sendSamplingRateCommand(samplingRate: Int, deviceAddress: String) {
        val command = "[a,$samplingRate]"
        sendCommand(command, deviceAddress)
    }
    //future function to send file
    fun sendFile(fileData: ByteArray) {
        TODO()
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }
    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    fun getBluetoothDevice(address: String): android.bluetooth.BluetoothDevice? {
        return bluetoothController.getDeviceByAddress(address)
    }

    private fun Flow<ConnectionResult>.listen(): Job {

        Log.d("we're out", "onEach dentro")
        return onEach { result ->
            Log.d("we're in", "onEach dentro")

            when (result) {

                ConnectionResult.ConnectionEstablished -> {
                    Log.d("BluetoothViewModel", "Connected to master")

                    _state.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    Log.e("BluetoothViewModel", "Connection error: ${result.message}")

                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ConnectionResult.StringReceived -> {
                    Log.d("BluetoothViewModel", "Received message from ${result.deviceAddress}: ${result.message}")
                    _state.update { currentState->
                        val currentMessages = currentState.messages[result.deviceAddress] ?: emptyList()

                        // Add the new message to the list for that device
                        val updatedMessages = currentMessages + result.message

                        val updatedMessagesMap = currentState.messages.toMutableMap().apply {
                            this[result.deviceAddress] = updatedMessages
                        }

                        currentState.copy(
                            messages = updatedMessagesMap,
                        )
                    }
                }

                is ConnectionResult.FileReceived -> TODO()

            }
        }.catch { throwable ->
            Log.e("BluetoothViewModel", "Error in connection flow: ${throwable.message}")
            bluetoothController.closeConnection()
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}



