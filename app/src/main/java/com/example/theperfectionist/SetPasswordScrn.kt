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
fun SetPasswordScrn(navController: NavController) {
    val context = LocalContext.current
    val passwordManager = remember { PasswordManager(context) }

    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(firstPin, confirmPin, isConfirming) {
        if (!isConfirming && firstPin.length == 6) {
            isConfirming = true
            error = ""
        } else if (isConfirming && confirmPin.length == 6) {
            if (firstPin == confirmPin) {
                passwordManager.savePassword(firstPin)
                navController.navigate("Bluetooth") {
                    popUpTo("set_password") { inclusive = true }
                }
            } else {
                error = "PINs do not match"
                firstPin = ""
                confirmPin = ""
                isConfirming = false
            }
        }
    }

    val activePin = if (isConfirming) confirmPin else firstPin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isConfirming) "Confirm Your 6-Digit PIN" else "Set Your 6-Digit PIN",
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            repeat(6) { index ->
                val filled = index < activePin.length
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

        PinPad(
            onDigitPressed = { digit ->
                if (!isConfirming) {
                    if (firstPin.length < 6) {
                        firstPin += digit
                        error = ""
                    }
                } else {
                    if (confirmPin.length < 6) {
                        confirmPin += digit
                        error = ""
                    }
                }
            },
            onDeletePressed = {
                if (!isConfirming) {
                    if (firstPin.isNotEmpty()) {
                        firstPin = firstPin.dropLast(1)
                        error = ""
                    }
                } else {
                    if (confirmPin.isNotEmpty()) {
                        confirmPin = confirmPin.dropLast(1)
                        error = ""
                    }
                }
            }
        )
    }
}

@Composable
private fun PinPad(
    onDigitPressed: (String) -> Unit,
    onDeletePressed: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        PinRow(listOf("1", "2", "3"), onDigitPressed)
        PinRow(listOf("4", "5", "6"), onDigitPressed)
        PinRow(listOf("7", "8", "9"), onDigitPressed)

        Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.size(85.dp))

            PinButton(text = "0") { onDigitPressed("0") }

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
private fun PinRow(
    digits: List<String>,
    onDigitPressed: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
        digits.forEach { digit ->
            PinButton(text = digit) { onDigitPressed(digit) }
        }
    }
}

@Composable
private fun PinButton(
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