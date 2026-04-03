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
fun StandIdeal(navController: NavController, mac: String) {
    var isRecording by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Standing Ideal", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "1) Stand with your back against a wall.\n" +
                    "2) Touch back of head, shoulder blades and buttocks to the wall.\n" +
                    "3) Keep heels 2–4 inches away from the wall.\n" +
                    "4) Place one hand behind your lower back.\n" +
                    "5) There should be a small natural gap.\n" +
                    "6) Gently tuck your chin.\n" +
                    "7) Step away from the wall and maintain this alignment."
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
                navController.navigate("sit_relaxed/$mac")
                isRecording = false
            }
        }
    }
}