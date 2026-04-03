package com.example.theperfectionist

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@SuppressLint("MissingPermission", "ContextCastToActivity")
@Composable
fun BluetoothScrn(navController: NavController) {

    Box(Modifier.fillMaxSize().background(Color(0xFFA2CCFF).copy(alpha = 0.85f)))

    val activity = LocalContext.current as MainActivity
    val bt = activity.btManager
    val isConnected = bt.isConnected // to check if the bluetooth is already connected

    if (isConnected) {
        Column(Modifier.fillMaxSize()/*.background(Color(0xFFA2CCFF).copy(alpha = 0.85f))*/,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already Connected to Bluetooth", color = Color.DarkGray
            )
        }
        return
    }


    var pairedDevices by remember { mutableStateOf(bt.getPairedDevices()?.toList() ?: emptyList<BluetoothDevice>()) }
    var availableDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    var bleDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }

/*    Row(modifier = Modifier.fillMaxWidth().padding(top = 30.dp, start = 10.dp, end = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) //This line will automatically adjust the button position based on phone screen size
    {
        IconButton(onClick = { navController.navigate("screen_3") }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Localized description"
            )
        }
    }*/

    @OptIn(ExperimentalMaterial3Api::class)
    CenterAlignedTopAppBar( //or replace this with just TopAppBar for regular (has the text on the left side)
        title = {
            Text(
                text = "Bluetooth Scanner",
                color = Color(0xFF003366),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("screen_3") }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF003366)
                )
            }
        },
        actions = {
            var expanded by remember { mutableStateOf(false) }

            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color(0xFF003366)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Account") },
                    onClick = { navController.navigate("Account") }
                )
/*                DropdownMenuItem(   //remove wifi section (most likely will not need it)
                    text = { Text("Wifi") },
                    onClick = { navController.navigate("WiFi") }
                )*/

                DropdownMenuItem(
                    text = { Text("Sound") },
                    onClick = { navController.navigate("Sound") }
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFFA2CCFF).copy(alpha = 0.85f),
            navigationIconContentColor = Color(0xFF003366),
            actionIconContentColor = Color(0xFF003366),
            titleContentColor = Color(0xFF003366)
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // HEADER + SCAN BUTTON
        item {
            Spacer(modifier = Modifier.height(60.dp))
            //Text("Bluetooth Scanner")
            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                availableDevices = emptyList()
                bleDevices = emptyList()

                bt.startDiscovery { device ->
                    availableDevices = availableDevices + device
                }

                bt.startBleScan { device ->
                    bleDevices = bleDevices + device
                }
            },

                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
                //elevation = ButtonDefaults.buttonElevation(8.dp),
                elevation = null,
                contentPadding = PaddingValues(16.dp))
            {
                Text("Scan for Devices", color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { navController.navigate("posture_history") },

                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
                elevation = null,
                contentPadding = PaddingValues(16.dp))
            {
                Text("View Saved Posture Graph", color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Paired Devices")
        }

        // CLASSIC PAIRED DEVICES
        items(pairedDevices) { device ->
            DeviceCard(device.name ?: "Unknown", device.address) {
                navController.navigate("calibration/${device.address}")
            }
        }

        // AVAILABLE DEVICES
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Available Devices")
        }

        items(availableDevices) { device ->
            DeviceCard(device.name ?: "Unknown", device.address) {
                navController.navigate("stand_normal/${device.address}")
            }
        }

        // BLE DEVICES
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("BLE Devices")
        }

        items(bleDevices) { device ->
            DeviceCard(device.name ?: "Unknown BLE Device", device.address) {
                navController.navigate("stand_normal/${device.address}")
            }
        }
    }
}

@Composable
fun DeviceCard(name: String, address: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = name)
            Text(text = address)
        }
    }
}
