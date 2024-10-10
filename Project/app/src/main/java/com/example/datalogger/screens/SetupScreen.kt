package com.example.datalogger.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datalogger.data.PreferencesManager

//setup screen with a text and a button
@Composable
fun SetupScreen(
    navController: NavController,
    preferencesManager: PreferencesManager,
    isSetupCompleted: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Setup Screen")
        //make "isSetupCompleted" in the shared preferences to true and navigate to setup
        //the "isSetupCompleted" variable is used for whenever the screen re-composes
        Button(
            onClick = {
                preferencesManager.setSetupComplete(true)
                isSetupCompleted.value = true
                navController.navigate("home")
            }
        ) {
            Text(text = "Done!")
        }
    }
}