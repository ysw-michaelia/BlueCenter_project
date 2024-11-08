package com.example.datalogger.state

import android.app.Application
import android.bluetooth.BluetoothSocket
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
import java.io.IOException
import javax.inject.Inject

//bluetooth viewmodel, similar dependency handling as setup viewmodel
class BluetoothViewModel (application: Application): AndroidViewModel(application) {
    private val sensorViewModel = SensorViewModel(application)

    private val clientDisconnect: (address: String) -> Unit = { address ->
        _state.update { currentState ->
            // Create new maps to hold the updated state
            val updatedInteractionLog = currentState.interactionLog.toMutableMap().apply {
                remove(address)  // Remove all messages for the disconnected address
            }
            val updatedReceivedMessages = currentState.interactionLog.toMutableMap().apply {
                remove(address)  // Remove all messages for the disconnected address
            }

            val updatedIsTalking = currentState.isTalking.toMutableMap().apply {
                this[address] = false  // Set isTalking to false for the disconnected address
            }

            // Return the updated state with modified interactionLog and isTalking
            currentState.copy(
                interactionLog = updatedInteractionLog,
                receivedMessages = updatedReceivedMessages,
                isTalking = updatedIsTalking
            )
        }
    }

    //controller needed for the view model, it's a singleton
    private val bluetoothController: BluetoothController = AndroidBluetoothController(application, sensorViewModel ,clientDisconnect)

    //state that holds the bluetooth devices, paired devices, etc.
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
            interactionLog = if(state.isServerOpen) state.interactionLog else emptyMap(),
            receivedMessages = if(state.isServerOpen) state.receivedMessages else emptyMap(),
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
            val bluetoothCommand = bluetoothController.trySendCommand(command, deviceAddress)
            if (bluetoothCommand != null) {
                _state.update { currentState->
                    val currentMessages = currentState.interactionLog[deviceAddress] ?: emptyList()

                    // Add the new message to the list for that device
                    val updatedCommands = currentMessages + "You: $bluetoothCommand"

                    val updatedCommandsMap = currentState.interactionLog.toMutableMap().apply {
                        this[deviceAddress] = updatedCommands
                    }
                    val updatedIsTalking = currentState.isTalking.toMutableMap().apply {
                        this[deviceAddress] = false  // Set isTalking to false for the disconnected address
                    }
                    currentState.copy(
                        interactionLog = updatedCommandsMap,
                        isTalking = updatedIsTalking
                    )
                }

            }
        }
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

        Log.d("listen", "start")
        return onEach { result ->
            Log.d("listen", "received")

            when (result) {

                is ConnectionResult.ConnectionEstablished -> {

                    _state.update { currentState ->
                        val updatedIsTalking = currentState.isTalking.toMutableMap().apply {
                            put(result.deviceAddress, false)  // Initialize the new device's status to false
                        }
                        currentState.copy(
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null,
                            isTalking = updatedIsTalking
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ConnectionResult.StringReceived -> {
                    val slaveName = bluetoothController.connectedDevices.value.find { it.address == result.deviceAddress }?.name ?: "Slave"
                    _state.update { currentState->
                        val currentInteractionLog = currentState.interactionLog[result.deviceAddress] ?: emptyList()

                        val message = result.message

                        // Add the new message to the list for that device
                        val updatedInteractionLog = currentInteractionLog + "$slaveName: \n$message"
                        val updatedReceivedMessages = mutableListOf(message)

                        val updatedInteractionLogMap = currentState.interactionLog.toMutableMap().apply {
                            this[result.deviceAddress] = updatedInteractionLog
                        }

                        val updatedReceivedMessagesMap = currentState.receivedMessages.toMutableMap().apply {
                            this[result.deviceAddress] = updatedReceivedMessages
                        }

                        val updatedIsTalking = currentState.isTalking.toMutableMap().apply {
                            this[result.deviceAddress] = false  // Set isTalking to false for the disconnected address
                        }

                        currentState.copy(
                            interactionLog = updatedInteractionLogMap,
                            receivedMessages = updatedReceivedMessagesMap,
                            isTalking = updatedIsTalking
                        )
                    }
                }

                is ConnectionResult.FileReceived -> {
                    _state.update { currentState ->
                        val currentMessages =
                            currentState.interactionLog[result.deviceAddress] ?: emptyList()

                        // Add the new message to the list for that device
                        val updatedMessages = currentMessages + "File ${result.file.name} received \n"

                        val updatedMessagesMap = currentState.interactionLog.toMutableMap().apply {
                            this[result.deviceAddress] = updatedMessages
                        }
                        val updatedIsTalking = currentState.isTalking.toMutableMap().apply {
                            this[result.deviceAddress] =
                                false  // Set isTalking to false for the disconnected address
                        }

                        currentState.copy(
                            interactionLog = updatedMessagesMap,
                            isTalking = updatedIsTalking
                        )
                    }
                }

            }
        }.catch { throwable ->
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



