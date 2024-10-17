package com.example.datalogger.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.RadioButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datalogger.state.SetupViewModel

//setup screen with a text and a button
@Composable
fun SetupScreen(
    navController: NavController,
    viewModel: SetupViewModel,
    modifier: Modifier = Modifier
) {
    //holds the role
    var selectedRole by remember { mutableStateOf("Master") }

    Text(
        modifier = modifier.padding(40.dp),
        text = "SETUP DEVICE",
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold
    )
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Choose Role")

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedRole == "Master",
                onClick = { selectedRole = "Master" }
            )
            Text("Master")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedRole == "Slave",
                onClick = { selectedRole = "Slave" }
            )
            Text("Slave")
        }
       if(selectedRole == "Slave") {
           SlaveSetupPart(viewModel, navController)
       }
        else {
            MasterSetupPart(viewModel, navController)
       }
    }
}

@Composable
fun SlaveSetupPart(
    viewModel: SetupViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    //holds the number of channels entered by the user
    var numberOfChannels by remember { mutableStateOf("") }
    //text field for number of channels insertion
    TextField(
        value = numberOfChannels,
        onValueChange = { numberOfChannels = it },
        label = { Text("Number of Channels (1-32)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(
        modifier = modifier.padding(16.dp)
    )
    //variable to enable or disable button depending on user input
    val isButtonEnabled = numberOfChannels.toIntOrNull()?.let { it in 1..32 } == true
    Button(
        onClick = {
            //save setup as completed in shared preferences
            viewModel.setSetupCompleted(true)
            //save number of channels in shared preferences
            viewModel.setNumberOfChannels(numberOfChannels)
            //set slave as setup
            viewModel.setMaster(false)
            //navigate to home screen
            navController.navigate("slave_home")
        },
        enabled = isButtonEnabled //enable or disable button depending on user input
    ) {
        Text(text = "Done!")
    }
}

@Composable
fun MasterSetupPart(
    viewModel: SetupViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            //save setup as completed in shared preferences
            viewModel.setSetupCompleted(true)
            //save master as setup
            viewModel.setMaster(true)
            //navigate to home screen
            navController.navigate("master_home")
        }
    ) {
        Text(text = "Done!")
    }
}