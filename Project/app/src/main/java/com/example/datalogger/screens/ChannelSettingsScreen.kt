@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.datalogger.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.widget.TimePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.RadioButton
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datalogger.data.Channel
import com.example.datalogger.state.ChannelViewModel

//screen for individual channel settings
@Composable
fun ChannelSettingsScreen(
    channelId: Int,
    navController: NavController,
    channelViewModel: ChannelViewModel,
    modifier: Modifier = Modifier
) {

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
            var dummySensorOutput by remember { mutableStateOf(channel.staticValue) }
            var newStartTime by remember { mutableStateOf(channel.startTime) }
            var newStopTime by remember { mutableStateOf(channel.stopTime) }
            var static by remember { mutableStateOf(channel.isStatic) }
            var begHour by remember { mutableStateOf(channel.startTime.split(":")[0]) }
            var begMinute by remember { mutableStateOf(channel.startTime.split(":")[1]) }
            var endHour by remember { mutableStateOf(channel.stopTime.split(":")[0]) }
            var endMinute by remember { mutableStateOf(channel.stopTime.split(":")[1]) }
            var triggered by remember { mutableStateOf(channel.hasTriggerLevel) }
            var newTriggerLevel by remember { mutableStateOf(channel.triggerLevel) }
            var firstError by remember { mutableStateOf(false) }
            var secondError by remember { mutableStateOf(false) }
            var newSubmission by remember { mutableStateOf(false) }
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
                            sensorType = editedSensorType,
                            isStatic = static,
                            staticValue = dummySensorOutput,
                            startTime = newStartTime,
                            stopTime = newStopTime,
                            ))
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = static == false,
                    onClick = {
                        static = false
                        dummySensorOutput = 0F
                    }
                )
                Text("Sensor")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = static == true,
                    onClick = {
                        static = true
                        editedSensorName = "None"
                        editedSensorType = 0
                    }
                )
                Text("Static Digit")
            }
            Row() {
                if (static) {
                    Text("Static digit: ")
                    //text field to change the static digit
                    TextField(
                        value = dummySensorOutput.toString(),
                        onValueChange = { dummySensorOutput = it.toFloat() },
                        label = { Text("Edit Static Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else{
                    Text("Sensor: ${channel.sensorName}")
                    //dropdown menu to choose sensor
                    SensorDropdownMenu(onSensorSelected = { selectedSensor ->
                        editedSensorName = selectedSensor.name
                        editedSensorType = selectedSensor.type
                    })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = triggered == false,
                    onClick = {
                        triggered = false
                        newTriggerLevel = 0F
                    }
                )
                Text("Start and Stop times")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = triggered == true,
                    onClick = {
                        triggered = true
                        newStartTime = "00:00"
                        newStopTime = "00:00"
                    }
                )
                Text("Trigger Level")
            }

            if(triggered) {
                Text("Trigger Level: ")
                //text field to change the static digit
                TextField(
                    value = dummySensorOutput.toString(),
                    onValueChange = { dummySensorOutput = it.toFloat() },
                    label = { Text("Edit Static Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else {
                Row {
                    Text("Beginning Timestamp: ")
                }
                Row {
                    //text field to change the start time
                    TextField(
                        value = begHour,
                        onValueChange = { begHour = it },
                        label = { Text("Edit Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1F)
                    )
                    Text(":")
                    TextField(
                        value = begMinute,
                        onValueChange = { begMinute = it },
                        label = { Text("Edit Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1F)
                    )
                }
                Row {
                    Text("Ending Timestamp: ")
                }
                Row {
                    //text field to change the timer
                    TextField(
                        value = endHour,
                        onValueChange = { endHour = it },
                        label = { Text("Edit Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1F)
                    )
                    Text(":")
                    TextField(
                        value = endMinute,
                        onValueChange = { endMinute = it },
                        label = { Text("Edit Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1F)
                    )

                }
                Row {
                    Button(onClick = {
                        if (begHour != "" &&  begMinute != "" && endHour != "" &&  endMinute != "" && begHour.all { c: Char -> c.isDigit() } && begMinute.all { c: Char -> c.isDigit()} && endHour.all { c: Char -> c.isDigit() } && endMinute.all { c: Char -> c.isDigit() }) {
                            if (begHour.toInt() >= 24 || begHour.toInt() < 0 || begMinute.toInt() >= 60 || begMinute.toInt() < 0) {
                                newSubmission = false
                                firstError = true
                            }
                            else {
                                firstError = false
                            }
                            if (endHour.toInt() >= 24 || endHour.toInt() < 0 || endMinute.toInt() >= 60 || endMinute.toInt() < 0) {
                                newSubmission = false
                                secondError = true
                            }
                            else {
                                secondError = false
                            }
                            if (!firstError && !secondError) {
                                if (begHour.length < 2) {
                                    begHour = "0$begHour"
                                }
                                if (begMinute.length < 2) {
                                    begMinute = "0$begMinute"
                                }
                                if (endHour.length < 2) {
                                    endHour = "0$endHour"
                                }
                                if (endMinute.length < 2) {
                                    endMinute = "0$endMinute"
                                }
                                newStartTime = "$begHour:$begMinute"
                                newStopTime = "$endHour:$endMinute"
                                newSubmission = true
                            }
                        }
                        else {
                            newSubmission = false
                            firstError =
                                if (begHour == "" || begMinute == "" || !begHour.all { c: Char -> c.isDigit() } || !begMinute.all { c: Char -> c.isDigit()}) {
                                    true
                                } else {
                                    false
                                }
                            secondError =
                                if (endHour == "" ||  endMinute == "" || !endHour.all { c: Char -> c.isDigit() } || !endMinute.all { c: Char -> c.isDigit() }) {
                                    true
                                } else {
                                    false
                                }
                        }

                    }) {
                        Text("Submit Timestamps")
                    }
                }
                if (firstError) {
                    Row {
                        Text("ERROR: Beginning time stamp invalid, hours must be between 0-23, minutes must be between 0-59, both must be whole numbers (no decimal), and must not be null")
                    }
                }
                if (secondError) {
                    Row {
                        Text("ERROR: End time stamp invalid, hours must be between 0-23, minutes must be between 0-59, both must be whole numbers (no decimal), and must not be null")
                    }
                }
                if (newSubmission) {
                    Row {
                        Text("New Start time: $newStartTime")
                    }
                    Row {
                        Text("New Stop time: $newStopTime")
                    }
                }
            }
            }

            //Question: Should the timestamp be based on a real clock or based on the sensor?


    }
}

//function that displays all sensors in a dropdown menu, copied from chatGPT
@Composable
fun SensorDropdownMenu(
    modifier: Modifier = Modifier,
    onSensorSelected: (Sensor) -> Unit
) {
    val context = LocalContext.current
    val availableSensors = getAvailableSensors(context)  // Get list of sensors

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



//function to get all available sensors
fun getAvailableSensors(context: Context): List<Sensor> {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    return sensorManager.getSensorList(Sensor.TYPE_ALL)
}