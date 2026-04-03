package com.example.theperfectionist

//import android.graphics.Color
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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun Screen1(navController: NavController)
{
    Box(Modifier.fillMaxSize().background(Color(0xFFA2CCFF).copy(alpha = 0.85f)))

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center)

    {
        Text(text = "Welcome to the Perfectionist")

        Button(onClick =
            {
            navController.navigate("screen_2")
            },

            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
            //elevation = ButtonDefaults.buttonElevation(8.dp),
            elevation = null,
            contentPadding = PaddingValues(16.dp)
        )
        {
            Text(text = "Tap to start", color = Color.DarkGray)
        }
    }
}