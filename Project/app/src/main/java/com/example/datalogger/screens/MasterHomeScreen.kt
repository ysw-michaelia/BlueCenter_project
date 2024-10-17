package com.example.datalogger.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datalogger.state.BluetoothViewModel
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SetupViewModel

//home screen for master
@Composable
fun MasterHomeScreen(
    navController: NavController,
    setupViewModel: SetupViewModel,
    channelViewModel: ChannelViewModel,
    bluetoothViewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MasterHomeTopBar(navController = navController)
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        },
                        modifier = Modifier.padding(top = 40.dp, start = 100.dp)
                    ) {
                        Text(text = "Reset")
                    }
                }
            }
        }
    )

}

//top bar for master home
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterHomeTopBar(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Master") },
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

                        navController.navigate("master_bluetooth_settings")
                    }
                )

            }
        }
    )
}
