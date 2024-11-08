package com.example.datalogger.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SetupViewModel
import java.io.File
import java.io.FileOutputStream

//setup screen with a text and a button
@Composable
fun SetupScreen(
    navController: NavController,
    viewModel: SetupViewModel,
    channelViewModel: ChannelViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    //holds the role
    var selectedRole by remember { mutableStateOf("Master") }

    //holds the timestamp
    var selectedTimeStamp by remember { mutableStateOf("Master Timestamp") }

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
        Text(text = "Step 1: Choose Role",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

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
        Spacer(
            modifier = modifier.padding(16.dp)
        )
        if (selectedRole == "Master") {
            Text(
                text = "Step 2: Master Configuration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text("Choose Timestamp Source")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedTimeStamp == "Master Timestamp",
                    onClick = { selectedTimeStamp = "Master Timestamp" }
                )
                Text("Master Timestamp")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedTimeStamp == "Slave Timestamp",
                    onClick = { selectedTimeStamp = "Slave Timestamp" }
                )
                Text("Slave Timestamp")
            }
            MasterSetupPart(viewModel, navController, role = selectedRole, timestamp = selectedTimeStamp)
        } else {
            Text(
                text = "Step 2: Slave Configuration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text("Fill in the required number of channels")
            SlaveSetupPart(viewModel, navController, role = selectedRole)
        }
    }
}

@Composable
fun SlaveSetupPart(
    viewModel: SetupViewModel,
    navController: NavController,
    role: String = "Slave",
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    val timestamp = "null"
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

            saveConfiguration(context, role, timestamp, numberOfChannels)

//            if (selectedTimeStamp == "Master Timestamp") {
//                channelViewModel.updateAllMasterTimestamps(true)
//            }
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
    role: String = "Master",
    timestamp: String,
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    val numberOfChannels = "null"
    Button(
        onClick = {
            //save setup as completed in shared preferences
            viewModel.setSetupCompleted(true)
            //save master as setup
            viewModel.setMaster(true)
            //navigate to home screen
            navController.navigate("master_home")

            saveConfiguration(context, role, timestamp, numberOfChannels)
        }
    ) {
        Text(text = "Done!")
    }
}

private var fileIndex = 1

// Method to create and write data to the file
fun saveConfiguration(context: Context, role: String, timestamp: String, numberOfChannels: String) {
    Log.d("saveConfiguration", "saveConfiguration called")
    val fileName = "configuration_${role}_$fileIndex.txt"

    // Get the app's internal storage directory
    val file = File(context.filesDir, fileName)

    // Open a FileOutputStream to write data to the file
    val fileOutputStream = FileOutputStream(file, true)

    // Format the data to be written
    val formattedData = "role $role\ntimestamp $timestamp\nnumberOfChannels $numberOfChannels\n"

    // Write the data to the file
    fileOutputStream.write(formattedData.toByteArray())
    fileOutputStream.flush()
    fileOutputStream.close()

    // Increment the file index for the next file
    fileIndex++
}