package com.example.theperfectionist

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun Screen2(navController: NavController) {

    Box(Modifier.fillMaxSize().background(Color(0xFFA2CCFF).copy(alpha = 0.85f)))

    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }.toTypedArray()

    var permissionsGranted by remember { mutableStateOf(false) }

    // 1. Declare Bluetooth enable launcher FIRST
    val enableBluetoothLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                navController.navigate("Bluetooth")
            }
        }




    // 2. Declare permission launcher AFTER, so it can call the Bluetooth launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            permissionsGranted = results.values.all { it }
            if (permissionsGranted) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableIntent)
            }
        }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(text = "Is this your first time using our product?")

        Button(onClick = {
            permissionLauncher.launch(permissions)
        },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
            //elevation = ButtonDefaults.buttonElevation(8.dp),
            elevation = null,
            contentPadding = PaddingValues(16.dp)

        ) {
            Text(text = "Yes", color = Color.DarkGray)
        }

        Button(onClick = {

            navController.navigate("screen_3")
        },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
            //elevation = ButtonDefaults.buttonElevation(8.dp),
            elevation = null,
            contentPadding = PaddingValues(16.dp)
            ) {
            Text(text = "No", color = Color.DarkGray)
        }

    }
}
