@file:Suppress("DEPRECATION")

package com.example.theperfectionist

import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    lateinit var btManager: BluetoothManager

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btManager = BluetoothManager(this)

        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val passwordManager = remember { PasswordManager(context) }

            val startDestination = if (passwordManager.hasPassword()) {
                "password"
            } else {
                "set_password"
            }

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable("set_password") { SetPasswordScrn(navController) }
                composable("password") { PasswordScrn(navController) }
                composable("change_password") { ChangePasswordScrn(navController) }

                composable("screen_1") { Screen1(navController) }
                composable("screen_2") { Screen2(navController) }
                composable("screen_3") { Screen3(navController) }
                composable("settings") { SettingScreen(navController) }
                composable("Account") { AccountScrn(navController) }
                composable("WiFi") { WiFiScrn(navController) }
                composable("Bluetooth") { BluetoothScrn(navController) }
                composable("posture_history") { PostureHistoryScrn(navController) }
                composable("Sound") { SoundScrn(navController) }

                composable("stand_normal/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac") ?: ""
                    StandNormal(navController = navController, mac = mac)
                }

                composable("stand_ideal/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac") ?: ""
                    StandIdeal(navController = navController, mac = mac)
                }

                composable("sit_relaxed/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac") ?: ""
                    SitNormal(navController = navController, mac = mac)
                }

                composable("sit_ideal/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac") ?: ""
                    SitIdeal(navController = navController, mac = mac)
                }

                composable("target_date/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac") ?: ""
                    TargetDateScrn(navController = navController, mac = mac)
                }

                composable("calibration/{mac}") { backStack ->
                    val mac = backStack.arguments?.getString("mac") ?: ""
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