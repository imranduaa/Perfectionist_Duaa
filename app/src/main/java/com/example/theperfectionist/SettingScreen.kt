package com.example.theperfectionist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun SettingScreen(navController: NavController)
{
    Box(Modifier.fillMaxSize().background(Color(0xFFA2CCFF).copy(alpha = 0.85f)))

    Row(Modifier.offset(0.dp, 30.dp))
    {
        IconButton(onClick = { navController.navigate("screen_3") }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Localized description"
            )
        }
    }
    Column(
        Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Settings")
        Button(onClick = {
            navController.navigate("Account")

        },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
            //elevation = ButtonDefaults.buttonElevation(8.dp),
            elevation = null,
            contentPadding = PaddingValues(16.dp))

        {
            Text(text = "Account", color = Color.DarkGray)
        }

        Button(onClick = {
            navController.navigate("WiFi")
        },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
            //elevation = ButtonDefaults.buttonElevation(8.dp),
            elevation = null,
            contentPadding = PaddingValues(16.dp))

        {
            Text(text = "WiFi", color = Color.DarkGray)
        }

        Button(onClick = {
            navController.navigate("Bluetooth")
        },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
            //elevation = ButtonDefaults.buttonElevation(8.dp),
            elevation = null,
            contentPadding = PaddingValues(16.dp))

        {
            Text(text = "Bluetooth", color = Color.DarkGray)
        }

        Button(onClick = {
            navController.navigate("Sound")
        },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
            //elevation = ButtonDefaults.buttonElevation(8.dp),
            elevation = null,
            contentPadding = PaddingValues(16.dp))

        {
            Text(text = "Sound", color = Color.DarkGray)
        }



    }
}