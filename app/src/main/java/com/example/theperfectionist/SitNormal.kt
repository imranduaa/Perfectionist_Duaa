package com.example.theperfectionist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SitNormal(navController: NavController, mac: String) {
    var isRecording by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sitting Relaxed", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Sit on a chair all the way back in a relaxed position like you normally do.")

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                isRecording = true
                message = "Recording..."
            },
            enabled = !isRecording
        ) {
            Text("Start Recording")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(message)

        if (isRecording) {
            LaunchedEffect(Unit) {
                delay(8000)
                message = "Recording Complete"
                delay(100)
                navController.navigate("sit_ideal/$mac")
                isRecording = false
            }
        }
    }
}