package com.example.datalogger.screens

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.datalogger.chart.ChannelData
import com.example.datalogger.chart.parseChannelData
import com.example.datalogger.state.BluetoothViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp


@Composable
fun GraphScreen(
    navController: NavController,
    bluetoothViewModel: BluetoothViewModel,
    device: BluetoothDevice
) {
    val state by bluetoothViewModel.state.collectAsState()
    val messages = state.receivedMessages[device.address] ?: emptyList()
    val dataMap = parseChannelData(messages)

    val chartWidth = 200f
    val chartHeight = 200f
    val padding = 16f
    val maxDataValue = dataMap.values.flatMap { it.xValues + it.yValues + (it.zValues ?: emptyList()) }.maxOrNull() ?: 1f
    Log.d("GraphDebug", "Data: $dataMap")

    Box(
        modifier = Modifier
            .fillMaxSize() // Ensures the Box takes up the entire screen
            .padding(16.dp), // Optional padding around the canvas
        contentAlignment = Alignment.Center // Centers the Canvas inside the Box
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row() {
                Text(
                    text = "Line Chart",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Button(
                    onClick = { navController.navigate("device_console/${device.address}") },
                modifier = Modifier.padding(start = 20.dp)) {
                    Text("Back")
                }
            }


            Spacer(modifier = Modifier.height(30.dp))
            // Draw the chart using Canvas
            Canvas(modifier = Modifier.size(chartWidth.dp, chartHeight.dp)) {
                // Draw each channel separately
                dataMap.forEach { (channelName, channelData) ->
                    // Colors for x, y, z series for differentiation
                    val xColor = Color.Red // Red for X values
                    val yColor = Color.Green // Green for Y values
                    val zColor = Color.Blue // Blue for Z values

                    // Determine the min and max values across all three axes (X, Y, Z) to normalize them
                    val minX = channelData.xValues.minOrNull() ?: 0f
                    val maxX = channelData.xValues.maxOrNull() ?: 1f
                    val minY = channelData.yValues.minOrNull() ?: 0f
                    val maxY = channelData.yValues.maxOrNull() ?: 1f
                    val minZ = channelData.zValues?.minOrNull() ?: 0f
                    val maxZ = channelData.zValues?.maxOrNull() ?: 1f

                    // Define max scale value based on the largest range
                    val maxDataValue = maxOf(maxX, maxY, maxZ)

                    // Helper function to scale data points to canvas dimensions
                    fun scale(value: Float, maxValue: Float): Float {
                        return (value / maxValue) * (size.height - padding * 2)
                    }

                    // Draw X values line
                    drawPath(
                        path = Path().apply {
                            channelData.xValues.forEachIndexed { index, xValue ->
                                val xPos = index.toFloat() / channelData.xValues.size * size.width
                                val yPos = size.height - padding - scale(xValue, maxDataValue) // Correct scaling
                                if (index == 0) moveTo(xPos, yPos) else lineTo(xPos, yPos)
                            }
                        },
                        color = xColor,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw Y values line
                    drawPath(
                        path = Path().apply {
                            channelData.yValues.forEachIndexed { index, yValue ->
                                val xPos = index.toFloat() / channelData.yValues.size * size.width
                                val yPos = size.height - padding - scale(yValue, maxDataValue) // Correct scaling
                                if (index == 0) moveTo(xPos, yPos) else lineTo(xPos, yPos)
                            }
                        },
                        color = yColor,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw Z values line if available
                    channelData.zValues?.let { zValues ->
                        drawPath(
                            path = Path().apply {
                                zValues.forEachIndexed { index, zValue ->
                                    val xPos = index.toFloat() / zValues.size * size.width
                                    val yPos = size.height - padding - scale(zValue, maxDataValue) // Correct scaling
                                    if (index == 0) moveTo(xPos, yPos) else lineTo(xPos, yPos)
                                }
                            },
                            color = zColor,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Optional: Draw Axes
                drawLine(
                    start = Offset(padding, size.height - padding),
                    end = Offset(size.width - padding, size.height - padding),
                    color = Color.Black,
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    start = Offset(padding, padding),
                    end = Offset(padding, size.height - padding),
                    color = Color.Black,
                    strokeWidth = 1.dp.toPx()
                )

                // Draw X-axis label
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "Time",  // Label text
                        size.width / 2, // X position (centered at the bottom of the canvas)
                        size.height - padding + 40, // Y position (slightly below the X-axis)
                        android.graphics.Paint().apply {
                            textSize = 40f // Set text size
                            color = android.graphics.Color.BLACK
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

                // Draw Y-axis label
                drawContext.canvas.nativeCanvas.apply {
                    save() // Save the current canvas state

                    // Move the canvas origin to the desired position for "Y-axis" text
                    translate(40f, size.height / 2) // Adjust X and Y position as needed

                    // Rotate the text 90 degrees counterclockwise
                    rotate(-90f)

                    drawText(
                        "Values", // Label text
                        0f, // X position (relative to the translated origin)
                        0f, // Y position (relative to the translated origin)
                        android.graphics.Paint().apply {
                            textSize = 40f // Set text size
                            color = android.graphics.Color.BLACK
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )

                    restore() // Restore the canvas state after drawing the rotated text
                }

            }



        }
    }
}