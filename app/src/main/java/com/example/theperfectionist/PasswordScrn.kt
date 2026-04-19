package com.example.theperfectionist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PasswordScrn(navController: NavController) {
    val context = LocalContext.current
    val passwordManager = remember { PasswordManager(context) }
    val activity = context as MainActivity
    val btManager = activity.btManager

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(pin) {
        if (pin.length == 6) {
            val savedPin = passwordManager.getPassword()
            if (pin == savedPin) {
                if (btManager.isConnected && btManager.connectedDevice != null) {
                    navController.navigate("screen_3") {
                        popUpTo("password") { inclusive = true }
                    }
                } else {
                    navController.navigate("Bluetooth") {
                        popUpTo("password") { inclusive = true }
                    }
                }
            } else {
                error = "Incorrect PIN"
                pin = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Enter PIN", color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            repeat(6) { index ->
                val filled = index < pin.length
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(
                            color = if (filled) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                )
                if (index < 5) Spacer(modifier = Modifier.width(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (error.isNotEmpty()) {
            Text(text = error, color = Color.Red)
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Spacer(modifier = Modifier.height(28.dp))
        }

        PasswordPinPad(
            onDigitPressed = { digit ->
                if (pin.length < 6) {
                    pin += digit
                    error = ""
                }
            },
            onDeletePressed = {
                if (pin.isNotEmpty()) {
                    pin = pin.dropLast(1)
                    error = ""
                }
            }
        )
    }
}

@Composable
private fun PasswordPinPad(
    onDigitPressed: (String) -> Unit,
    onDeletePressed: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        PasswordPinRow(listOf("1", "2", "3"), onDigitPressed)
        PasswordPinRow(listOf("4", "5", "6"), onDigitPressed)
        PasswordPinRow(listOf("7", "8", "9"), onDigitPressed)

        Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.size(85.dp))

            PasswordPinButton(text = "0") { onDigitPressed("0") }

            Button(
                onClick = onDeletePressed,
                modifier = Modifier.size(85.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("⌫", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun PasswordPinRow(
    digits: List<String>,
    onDigitPressed: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
        digits.forEach { digit ->
            PasswordPinButton(text = digit) { onDigitPressed(digit) }
        }
    }
}

@Composable
private fun PasswordPinButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(85.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.primary, fontSize = 24.sp)
    }
}