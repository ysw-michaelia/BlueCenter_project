package com.example.datalogger.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Slider
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import com.example.datalogger.data.PreferencesManager
import com.example.datalogger.state.SetupViewModel

//setup screen with a text and a button
@Composable
fun SetupScreen(
    navController: NavController,
    viewModel: SetupViewModel,
    modifier: Modifier = Modifier
) {
    //holds the number of channels entered by the user
    var numberOfChannels by remember { mutableStateOf("") }
    //holds the error message (error handling copied by chatGPT, but it looked kind of useless)
    val errorMessage by viewModel.errorMessage.observeAsState()

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

        //text field for number of channels insertion
        TextField(
            value = numberOfChannels,
            onValueChange = { numberOfChannels = it },
            label = { Text("Number of Channels (1-32)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        //error message in case of channels selected not in range (1-32)
        //but it actually does not work properly so it's kind of useless
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }
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
                //navigate to home screen
                navController.navigate("home")
            },
            enabled = isButtonEnabled //enable or disable button depending on user input
        ) {
            Text(text = "Done!")
        }
    }
}