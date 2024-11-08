package com.example.datalogger.network

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.example.datalogger.state.SensorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.util.UUID

//implementation of the bluetooth controller interface
//it takes care of any interactions of bluetooth (scanning, pairing, connecting, etc.)
@SuppressLint ("MissingPermission")
class AndroidBluetoothController(
    private val context: Context,
    private val sensorViewModel: SensorViewModel,
    private val clientDisconnectCallback: (address: String) -> Unit
): BluetoothController {

    //manager and adapter for bluetooth
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val commandInterpeter = CommandInterpreter(sensorViewModel)

    //it checks if the device is connected
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    //it saves all scanned devices (used only on slave)
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scanDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val activeConnections = mutableMapOf<String, BluetoothSocket>()

    //it saves all paired devices (used only on slave)
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    //it saves all connected devices (used only on master)
    private val _connectedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val connectedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _connectedDevices.asStateFlow()

    private val onClientDisconnect: (socket: BluetoothSocket) -> Unit = { socket->
        connectedClients.remove(socket)
        _connectedDevices.update { currentDevices ->
            currentDevices.filterNot { it.address == socket.remoteDevice.address }
        }
        activeConnections[socket.remoteDevice.address]?.close()
        activeConnections.remove(socket.remoteDevice.address)
        dataTransferServices.remove(socket.remoteDevice.address)
        clientDisconnectCallback(socket.remoteDevice.address)
        try {
            socket.close()
        } catch (e: IOException) {
            Log.e("BluetoothService", "Error closing socket: ${e.message}")
        }
    }

    private val dataTransferServices = mutableMapOf<String, BluetoothDataTransferService>()

    //actual clients (sockets) connected
    private val connectedClients = mutableListOf<BluetoothSocket>()

    //possible errors
    private val _errors = MutableStateFlow<String?>(null)
    override val errors: StateFlow<String?>
        get() = _errors.asStateFlow()

    //whenever a new device is found
    private val foundDeviceReceiver = FoundDeviceReceiver { device->
        _scannedDevices.update { devices->
            val newDevice = device.toBluetoothDeviceDomain()
            if(newDevice in devices) devices else devices + newDevice
        }
    }


    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if(bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.tryEmit("Can't connect to a non-paired device")
            }
        }
    }

    //sockets needed for connection
    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    //function for discovery of nearby devices
    override fun startDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
            }
        )

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()

    }

    //function to stop the discovery of nearby devices
    override fun stopDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    //function that starts bluetooth server (only for master)
    //it makes the master hold a server where multiple slave sockets can connect to it
    override fun startBluetoothServer(): Flow<ConnectionResult> = channelFlow {
        // Ensure permissions
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            throw SecurityException("No permission")
        }

        // Listen on the server socket
        currentServerSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
            "data_sharing_service",
            UUID.fromString(SERVICE_UUID)
        )

        var isServerOpen = true // Server connection state

        while (isServerOpen) {
            try {
                // Accept client connection
                val clientSocket = currentServerSocket?.accept() ?: continue

                Log.d("BluetoothService", "Client connected: ${clientSocket.remoteDevice.name} at address: ${clientSocket.remoteDevice.address}")


                send(ConnectionResult.ConnectionEstablished(clientSocket.remoteDevice.address))
                connectedClients.add(clientSocket)
                _connectedDevices.update { it + clientSocket.remoteDevice.toBluetoothDeviceDomain() }
                activeConnections[clientSocket.remoteDevice.address] = clientSocket

                // Process each client connection
                launch {
                    val device = clientSocket.remoteDevice
                    val service = BluetoothDataTransferService(clientSocket, context, commandInterpeter, onClientDisconnect)
                    dataTransferServices[device.address] = service

                    // Handle incoming data from client
                    service.listenForIncomingString().collect { incomingString ->
                        Log.d("BluetoothService", "Received string: $incomingString from ${device.address}")
                        send(ConnectionResult.StringReceived(incomingString, device.address))
                    }

                    service.listenForIncomingFile().collect { incomingFile ->
                        Log.d("BluetoothService", "Received file ")
                        send(ConnectionResult.FileReceived(incomingFile, device.address))
                    }
                }

            } catch (e: IOException) {
                send(ConnectionResult.Error("Server socket error: ${e.message}"))
                closeConnection()
                isServerOpen = false // End the loop if an error occurs
            }
        }
    }.flowOn(Dispatchers.IO)

    //function to connect to a master (used by slave)
    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
                !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)

            currentClientSocket = bluetoothDevice
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )

            stopDiscovery()

            if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == false) {
                bluetoothDevice?.createBond()
            }

            //it has timeout
            currentClientSocket?.let { socket ->
                try {
                    withContext(Dispatchers.IO) {
                        withTimeout(10000L) { // 10 seconds timeout
                            socket.connect() // Attempt connection
                        }
                    }

                    val service = BluetoothDataTransferService(socket, context, commandInterpeter, onClientDisconnect)
                    dataTransferServices[device.address] = service

                    // Use withTimeout to avoid hanging indefinitely
                    emit(ConnectionResult.ConnectionEstablished(device.address))

                    emitAll(
                        service
                            .listenForIncomingCommand()
                            .map {
                                ConnectionResult.StringReceived(it, device.address)
                            }
                    )

                } catch (e: IOException) {
                    // Handle connection errors
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted: ${e.message}"))
                } catch (timeoutException: TimeoutCancellationException) {
                    // Handle timeout error
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection timed out"))
                }
            }
        }

    }

    override suspend fun trySendCommand(command: String, deviceAddress: String): String? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            return null
        }

        val service = dataTransferServices[deviceAddress]

        if(service == null) {
            return null
        }

        service.sendString(command.toByteArray())

        return command
    }

    override suspend fun trySendFile(fileData: ByteArray, deviceAddress: String): String? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            return null
        }

        val service = dataTransferServices[deviceAddress]

        if(service == null) {
            return null
        }


        return "File sent successfully"

    }

    override suspend fun trySendStringReply(message: String, deviceAddress: String): String? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            return null
        }

        val service = dataTransferServices[deviceAddress] ?: return null

        service.sendString(message.toByteArray())

        return message
    }

    override suspend fun trySendTimeStamp(deviceAddress: String): Boolean {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            return false
        }

        val service = dataTransferServices[deviceAddress] ?: return false

        service.sendTimestamp()
        return true
    }

    override suspend fun tryAskTimeStamp(deviceAddress: String): Boolean {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            return false
        }

        val service = dataTransferServices[deviceAddress] ?: return false

        service.requestTimestamp()
        return true

    }

    override fun getDeviceByAddress(address: String): BluetoothDevice? {
        return bluetoothAdapter?.getRemoteDevice(address)
    }

    //close the connection
    override fun closeConnection() {
        connectedClients.forEach { it.close() }
        connectedClients.clear()
        _connectedDevices.update { emptyList() }
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    fun getSocketForDevice(deviceAddress: String): BluetoothSocket? {
        return activeConnections[deviceAddress]
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    private fun updatePairedDevices() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map {
                it.toBluetoothDeviceDomain()
            }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }
    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    }

    companion object {
        const val SERVICE_UUID = "068b63e5-c6d7-44d1-a61f-e8d5c83739e3"
    }
}