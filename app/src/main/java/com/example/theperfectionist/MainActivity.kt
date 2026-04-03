@file:Suppress("DEPRECATION")

package com.example.theperfectionist


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi


class MainActivity : ComponentActivity() {

    lateinit var btManager: BluetoothManager

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btManager = BluetoothManager(this)

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "screen_1"
            ) {
                composable("screen_1") { Screen1(navController) }
                composable("screen_2") { Screen2(navController) }
                composable("screen_3") { Screen3(navController) }
                composable("settings") { SettingScreen(navController) }
                // composable("good") { GoodButton(navController) }
                // composable("bad") { BadButton(navController) }
                composable("Account") { AccountScrn(navController) }
                composable("WiFi") { WiFiScrn(navController) }
                composable("Bluetooth") { BluetoothScrn(navController) }
                composable("posture_history") { PostureHistoryScrn(navController) }
                composable("Sound") { SoundScrn(navController) }

                composable("stand_normal/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac")!!
                    StandNormal(navController = navController, mac = mac)
                }

                composable("stand_ideal/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac")!!
                    StandIdeal(navController = navController, mac = mac)
                }

                composable("sit_relaxed/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac")!!
                    SitNormal(navController = navController, mac = mac)
                }

                composable("sit_ideal/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac")!!
                    SitIdeal(navController = navController, mac = mac)
                }

                composable("calibration/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac")!!
                    val adapter = BluetoothAdapter.getDefaultAdapter()
                    val device = adapter.getRemoteDevice(mac)

                    CalibrationScrn(
                        device = device,
                        navController = navController,
                        showHome = true
                    )
                }
            }
        }
    }
}
