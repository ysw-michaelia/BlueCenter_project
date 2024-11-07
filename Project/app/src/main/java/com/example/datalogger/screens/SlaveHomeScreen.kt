package com.example.datalogger.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datalogger.data.Channel
import com.example.datalogger.state.BluetoothViewModel
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SensorViewModel
import com.example.datalogger.state.SetupViewModel

@Composable
fun SlaveHomeScreen(
    navController: NavController,
    setupViewModel: SetupViewModel,
    channelViewModel: ChannelViewModel,
    bluetoothViewModel: BluetoothViewModel,
    sensorViewModel: SensorViewModel,
    modifier: Modifier = Modifier
) {
    //needed values to know the state of the database
    val channelCount by channelViewModel.channelCount.observeAsState()
    val channelList by channelViewModel.channelList.observeAsState(emptyList())
    val actualChannelCount by setupViewModel.totChannels.observeAsState(0)

    //if the database is empty, create the channels, using launched effect to avoid
    //unexpected behaviour
    Scaffold(
        topBar = {
            SlaveHomeTopBar(navController = navController) // Call your custom TopBar
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize() // Ensures the Column takes full available height
                    .padding(paddingValues) // Apply padding from the Scaffold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(), // Ensures Row takes full width
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = modifier.padding(40.dp),
                        text = "HOME",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        //reset the settings in the shared preferences and navigate to setup
                        //also deletes all channels in the database
                        onClick = {
                            setupViewModel.clearPreferences()
                            channelViewModel.deleteChannels()
                            bluetoothViewModel.disconnectFromDevice()
                            navController.navigate("setup")
                            sensorViewModel.stopAllSensors()
                        },
                        modifier = Modifier.padding(top = 40.dp, start = 100.dp)
                    ) {
                        Text(text = "Reset")
                    }
                }
                LaunchedEffect(channelCount) {
                    if (channelCount == 0) {
                        channelViewModel.createChannelList(actualChannelCount)
                    }
                }
                //display all the channels in a lazy column
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    //get list of channels
                    channelViewModel.getChannelList()

                    items(channelList) { channel ->
                        ChannelCard(
                            channel = channel,
                            onLongPress = { selectedChannel ->
                                //if any channel is long pressed, navigate to channel settings
                                navController.navigate("channel_settings/${selectedChannel.channelId}")
                            },
                            modifier = Modifier.fillMaxSize(),
                            onCheckedChange = { isChecked ->
                                sensorViewModel.toggleMonitoring(channel.channelId, isChecked) // Call toggleMonitoring
                            }
                        )
                    }
                }
                Button(
                    onClick = {
                        // CHANGE THIS TO SAVE SETTINGS
                    },
                    modifier = Modifier.padding(top = 40.dp, start = 100.dp)
                ) {
                    Text(text = "Save settings")
                }
            }
        }
    )
}


//display info about the channel (for now it's just the name)
@Composable
fun ChannelCard(
    channel: Channel,
    onLongPress: (Channel) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier
) {
    var isChecked by remember { mutableStateOf(channel.isActivated) }

     Box(
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress(channel) //whenever longpressed, goes back and returns channel
                    }
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = channel.name,
                fontSize = 20.sp
            )
            Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                    isChecked = checked
                    onCheckedChange(checked)
                }
            )
        }
    }
}

//slave top bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlaveHomeTopBar(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Slave") },
        actions = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Bluetooth Settings") },
                    onClick = {
                        expanded = false
                        //navigate
                        navController.navigate("slave_bluetooth_settings")
                    }
                )

            }
        }
    )
}