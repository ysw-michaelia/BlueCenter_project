package com.example.datalogger.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datalogger.data.Channel
import com.example.datalogger.sensor.SensorController
import com.example.datalogger.state.ChannelViewModel

//screen for individual channel settings
@Composable
fun ChannelSettingsScreen(
    channelId: Int,
    navController: NavController,
    channelViewModel: ChannelViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sensorController = remember { SensorController(context) }
    var selectedSensor by remember { mutableStateOf<Sensor?>(null) }
    var isSensorRecording by remember { mutableStateOf(false) }

    //get the channel by id
    val channel = channelViewModel.getChannelById(channelId).collectAsState(initial = null).value
    Column(
        modifier = Modifier.padding(16.dp)
            .fillMaxSize()
    ) {
        //if channel is null, wait and show loading message
        if (channel == null) {
            Text(
                "Loading channel data...",
                modifier = Modifier.padding(40.dp)
            )
        } else {
            //variables that hold any edit to the channel
            var editedChannelName by remember { mutableStateOf(channel.name) }
            var editedSensorType by remember { mutableStateOf(channel.sensorType) }
            var editedSensorName by remember { mutableStateOf(channel.sensorName) }

            Row() {
                Text(
                    text = "${channel.name} settings: ",
                    fontSize = 24.sp
                )
                Button(
                    onClick = {
                        //change screen, save channel and might also need further changes
                        channelViewModel.upsertChannel(Channel(
                            channelId = channelId,
                            name = editedChannelName,
                            sensorName = editedSensorName,
                            sensorType = editedSensorType))
                        navController.navigate("slave_home")
                    }
                ) {
                    Text(text = "Save")
                }
            }

            //info about the channel
            Row() {
                Text("Channel Name: ")
                //text field to change the channel name
                TextField(
                    value = editedChannelName,
                    onValueChange = { editedChannelName = it },
                    label = { Text("Edit Channel Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row() {
                Text("Sensor: ${channel.sensorName}")
                //dropdown menu to choose sensor
                SensorDropdownMenu(
                    sensorController = sensorController,
                    onSensorSelected = { sensor ->
                    selectedSensor = sensor
                    editedSensorName = sensor.name
                    editedSensorType = sensor.type
                })
            }
            Row() {
                Button(
                    onClick = {
                        //start the sensor
                        selectedSensor?.let { sensor ->
                            if (!isSensorRecording) {
                                sensorController.startSensor(sensor) { sensorData ->
                                    Log.d("SensorData", sensorData.joinToString(", "))
                                }
                                isSensorRecording = true
                            }
                        }
                    },
                    enabled = !isSensorRecording
                ) {
                    Text("Start")
                }

                Button(
                    onClick = {
                        //Stop the sensor
                        if (isSensorRecording) {
                            sensorController.stopSensor()
                            isSensorRecording = false
                        }
                    },
                    enabled = isSensorRecording
                ) {
                    Text("Stop")
                }

                Button(
                    onClick = {
                        //start the sensor and record only once
                    }
                ) {
                    Text("Once")
                }
            }
        }
    }
}

//function that displays all sensors in a dropdown menu, copied from chatGPT
@Composable
fun SensorDropdownMenu(
    sensorController: SensorController,
    modifier: Modifier = Modifier,
    onSensorSelected: (Sensor) -> Unit,
) {
    val availableSensors by remember {
        mutableStateOf(sensorController.getAvailableSensors())
    }
    var expanded by remember { mutableStateOf(false) }  // Control menu expansion
    var selectedSensor by remember { mutableStateOf<Sensor?>(null) }  // Track selected sensor

    Column(modifier = modifier) {
        // Display selected sensor or prompt to select one
        Text(
            text = selectedSensor?.name ?: "Select a Sensor",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(16.dp)
        )

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableSensors.forEach { sensor ->
                DropdownMenuItem(
                    text = { Text(sensor.name) },
                    onClick = {
                        selectedSensor = sensor
                        expanded = false
                        onSensorSelected(sensor)
                    }
                )
            }
        }
    }
}