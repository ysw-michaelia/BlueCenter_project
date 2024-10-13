package com.example.datalogger.screens

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.example.datalogger.data.Channel
import com.example.datalogger.state.ChannelViewModel
import com.example.datalogger.state.SetupViewModel

//function that displays a text and a button
@Composable
fun HomeScreen(
    navController: NavController,
    setupViewModel: SetupViewModel,
    channelViewModel: ChannelViewModel,
    modifier: Modifier = Modifier
) {

    //needed values to know the state of the database
    val channelCount by channelViewModel.channelCount.observeAsState()
    val channelList by channelViewModel.channelList.observeAsState(emptyList())
    val actualChannelCount by setupViewModel.totChannels.observeAsState(0)

    Column() {
        Row() {
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
                    setupViewModel.setSetupCompleted(false)
                    channelViewModel.deleteChannels()
                    navController.navigate("setup")
                },
                modifier = Modifier.padding(top = 40.dp, start = 100.dp)
            ) {
                Text(text = "Reset")
            }

        }
        //if the database is empty, create the channels, using launched effect to avoid
        //unexpected behaviour
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

            items(channelList) { channel->
                ChannelCard(
                    channel = channel,
                    onLongPress = { selectedChannel ->
                        //if any channel is long pressed, navigate to channel settings
                        navController.navigate("channel_settings/${selectedChannel.channelId}")
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    }
}

//display info about the channel (for now it's just the name)
@Composable
fun ChannelCard(
    channel: Channel,
    onLongPress: (Channel) -> Unit,
    modifier: Modifier
) {
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
        Text(
            text = channel.name,
            fontSize = 20.sp
        )
    }
}