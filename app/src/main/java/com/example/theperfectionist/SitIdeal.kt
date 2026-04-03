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
fun SitIdeal(navController: NavController, mac: String) {
    var isRecording by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sitting Ideal", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "1) Sit all the way back in your chair so your back touches the backrest.\n" +
                    "2) Place a small rolled towel or cushion at your lower back.\n" +
                    "3) Keep feet flat on the floor and knees hip-width apart.\n" +
                    "4) Relax your shoulders downward.\n" +
                    "5) Bring your head back so your ears are aligned over your shoulders.\n" +
                    "6) Do not over-straighten. Stay comfortable and breathe normally."
        )

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
                navController.navigate("calibration/$mac")
                isRecording = false
            }
        }
    }
}