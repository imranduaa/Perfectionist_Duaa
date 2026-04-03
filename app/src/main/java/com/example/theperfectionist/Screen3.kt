package com.example.theperfectionist

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen3(navController: NavController) {

    var showCalibration by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { Screen3TopBar(navController) },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                onCalibrateSelected = { showCalibration = true }
            )
        }
    ) { innerPadding ->

        val context = LocalContext.current
        val activity = context as? MainActivity
        val bt = activity?.btManager
        val connectedDevice = bt?.connectedDevice

        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (showCalibration && connectedDevice != null) {
                CalibrationScrn(
                    navController = navController,
                    device = connectedDevice,
                    showHome = false
                )
            } else {
                Screen3Content(navController)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen3TopBar(navController: NavController) {

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "",
                color = Color(0xFF003366),
                style = MaterialTheme.typography.titleLarge
            )
        },
      /*  navigationIcon = {
            IconButton(onClick = { navController.navigate("screen_2") }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF003366)
                )
            }
        },*/
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
                DropdownMenuItem(text = { Text("Account") }, onClick = { navController.navigate("Account") })
                //DropdownMenuItem(text = { Text("Wifi") }, onClick = { navController.navigate("WiFi") })
                DropdownMenuItem(text = { Text("Bluetooth") }, onClick = { navController.navigate("Bluetooth") })
                DropdownMenuItem(text = { Text("Sound") }, onClick = { navController.navigate("Sound") })
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF8DBEF8).copy(alpha = 0.85f),  // Your unique top bar color
            navigationIconContentColor = Color(0xFF003366),
            actionIconContentColor = Color(0xFF003366),
            titleContentColor = Color(0xFF003366)
        )
    )
}


@Composable
fun BottomNavBar(
    navController: NavController,
    onCalibrateSelected: () -> Unit
) {

    val context = LocalContext.current
    val activity = context as? MainActivity
    val bt = activity?.btManager
    val isConnected = bt?.isConnected == true
    val connectedDevice = bt?.connectedDevice
    var notificationCount by remember { mutableStateOf(0) } //the number value is the number of notifcation at start

    NavigationBar(
        containerColor = Color(0xFF8DBEF8).copy(alpha = 0.85f)
    ) {

        NavigationBarItem(
            selected = false,
            onClick = {
                if (isConnected && connectedDevice != null) {
                    onCalibrateSelected()
                } else {
                    navController.navigate("Bluetooth")
                }
            },
            icon = {Icon(imageVector = Icons.Filled.Check, contentDescription = "Analytics")},
            label = { Text("Calibrate", color = Color.DarkGray) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("posture_history") }, //Make a Chart screen to replace
            icon = {Icon(imageVector = Icons.Filled.List, contentDescription = "Analytics")},
            label = { Text("Chart Log", color = Color.DarkGray) }
        )


        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("template2") },
            icon = {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge {
                                Text(notificationCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.DarkGray
                    )
                }
            },
            label = { Text("Notification", color = Color.DarkGray) }
        )

    }
}


@Composable
fun Screen3Content(navController: NavController) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFA2CCFF).copy(alpha = 0.85f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Select an option from the navigation bar below",
            color = Color.DarkGray
        )
    }
}
