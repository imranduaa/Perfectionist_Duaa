package com.example.theperfectionist

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.nio.charset.Charset
import java.util.UUID

@Composable
fun CalibrationScrn(
    device: BluetoothDevice,
    onDisconnect: () -> Unit = {},
    navController: NavController,
    showHome: Boolean
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFA2CCFF).copy(alpha = 0.85f))
    )

    val context = LocalContext.current
    val storage = remember { PostureStorage(context) }

    val serviceUuid = UUID.fromString("12345678-1234-1234-1234-1234567890ab")
    val charUuid = UUID.fromString("abcdefab-1234-1234-1234-abcdefabcdef")
    val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    var gatt by remember { mutableStateOf<BluetoothGatt?>(null) }

    var status by remember { mutableStateOf("Connecting...") }
    var roll by remember { mutableStateOf("--") }
    var pitch by remember { mutableStateOf("--") }
    var postureStateText by remember { mutableStateOf("--") }
    var familyText by remember { mutableStateOf("--") }
    var savedCount by remember { mutableStateOf(storage.sampleCount()) }
    var lastSavedTime by remember { mutableStateOf(storage.latestSavedTimeText()) }
    var lastSavedMillis by remember { mutableLongStateOf(0L) }

    val activity = context as MainActivity
    val bt = activity.btManager

    val gattCallback = remember {
        object : BluetoothGattCallback() {

            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(
                g: BluetoothGatt,
                statusCode: Int,
                newState: Int
            ) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    status = "Connected. Discovering services..."
                    bt.isConnected = true
                    bt.connectedDevice = device
                    g.discoverServices()
                } else {
                    status = "Disconnected"
                }
            }

            @SuppressLint("MissingPermission")
            override fun onServicesDiscovered(
                g: BluetoothGatt,
                statusCode: Int
            ) {
                val service = g.getService(serviceUuid)
                val characteristic = service?.getCharacteristic(charUuid)

                if (characteristic == null) {
                    status = "Characteristic not found"
                    return
                }

                g.setCharacteristicNotification(characteristic, true)

                val cccd = characteristic.getDescriptor(cccdUuid)
                if (cccd == null) {
                    status = "Notification descriptor not found"
                    return
                }

                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                g.writeDescriptor(cccd)
                status = "Receiving data..."
            }

            override fun onCharacteristicChanged(
                g: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                if (characteristic.uuid != charUuid) return

                val text = characteristic.value.toString(Charset.forName("UTF-8")).trim()
                val parsed = parseIncomingPayload(text)

                if (parsed == null) {
                    status = "Bad data: $text"
                    return
                }

                roll = String.format("%.2f", parsed.roll)
                pitch = String.format("%.2f", parsed.pitch)
                postureStateText = parsed.postureState
                familyText = parsed.family

                val now = System.currentTimeMillis()
                if (now - lastSavedMillis >= 2000L) {
                    lastSavedMillis = now

                    storage.appendSample(
                        PostureSample(
                            timestamp = now,
                            roll = parsed.roll,
                            pitch = parsed.pitch,
                            family = parsed.family,
                            postureState = parsed.postureState
                        )
                    )

                    savedCount += 1
                    lastSavedTime = storage.latestSavedTimeText()
                }
            }
        }
    }

    LaunchedEffect(device) {
        gatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        bt.isConnected = false
        bt.connectedDevice = null
        status = "Disconnected"
        onDisconnect()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showHome) {
            IconButton(onClick = { navController.navigate("screen_3") }) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home"
                )
            }
        }

        Text("Calibration", style = MaterialTheme.typography.headlineSmall)

        OutlinedButton(
            onClick = { disconnect() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Disconnect", color = Color.DarkGray)
        }

        OutlinedButton(
            onClick = { navController.navigate("posture_history") },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("View Today's Graph", color = Color.DarkGray)
        }

        Text("Status: $status")
        Spacer(Modifier.height(8.dp))

        Text("Roll: $roll°", style = MaterialTheme.typography.headlineMedium)
        Text("Pitch: $pitch°", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Posture State: $postureStateText",
            style = MaterialTheme.typography.headlineMedium
        )
        Text("Family: $familyText", style = MaterialTheme.typography.headlineSmall)
        Text("Saved samples on phone: $savedCount", style = MaterialTheme.typography.bodyLarge)
        Text("Last saved: $lastSavedTime", style = MaterialTheme.typography.bodyLarge)
    }
}

private data class ParsedBleReading(
    val roll: Float,
    val pitch: Float,
    val family: String,
    val postureState: String
)

private fun parseIncomingPayload(text: String): ParsedBleReading? {
    val parts = text.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    if (parts.size < 4) return null

    val roll = parts[0].toFloatOrNull() ?: return null
    val pitch = parts[1].toFloatOrNull() ?: return null
    val family = parts[2]
    val postureState = parts[3]

    return ParsedBleReading(
        roll = roll,
        pitch = pitch,
        family = family,
        postureState = postureState
    )
}