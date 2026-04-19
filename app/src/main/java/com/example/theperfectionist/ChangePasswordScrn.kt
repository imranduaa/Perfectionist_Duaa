package com.example.theperfectionist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun ChangePasswordScrn(navController: NavController) {
    val context = LocalContext.current
    val passwordManager = remember { PasswordManager(context) }

    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(currentPin, newPin, confirmPin, stage) {
        when (stage) {
            0 -> {
                if (currentPin.length == 6) {
                    if (currentPin == passwordManager.getPassword()) {
                        stage = 1
                        message = ""
                    } else {
                        message = "Current PIN is wrong"
                        currentPin = ""
                    }
                }
            }
            1 -> {
                if (newPin.length == 6) {
                    stage = 2
                    message = ""
                }
            }
            2 -> {
                if (confirmPin.length == 6) {
                    if (newPin == confirmPin) {
                        passwordManager.savePassword(newPin)
                        message = "PIN changed successfully"
                        currentPin = ""
                        newPin = ""
                        confirmPin = ""
                        stage = 0
                    } else {
                        message = "New PINs do not match"
                        newPin = ""
                        confirmPin = ""
                        stage = 1
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Text(
                text = "Change Password",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(18.dp))

            ChangePinField("Current PIN", currentPin.length, stage == 0)
            Spacer(modifier = Modifier.height(12.dp))
            ChangePinField("New 6-digit PIN", newPin.length, stage == 1)
            Spacer(modifier = Modifier.height(12.dp))
            ChangePinField("Confirm New PIN", confirmPin.length, stage == 2)

            Spacer(modifier = Modifier.height(16.dp))

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (message.contains("success")) Color(0xFF4CAF50) else Color.Red
                )
            } else {
                Text(
                    text = when (stage) {
                        0 -> "Enter your current PIN"
                        1 -> "Enter your new PIN"
                        else -> "Confirm your new PIN"
                    },
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ChangePasswordPinPad(
                onDigitPressed = { digit ->
                    when (stage) {
                        0 -> if (currentPin.length < 6) currentPin += digit
                        1 -> if (newPin.length < 6) newPin += digit
                        2 -> if (confirmPin.length < 6) confirmPin += digit
                    }
                    message = ""
                },
                onDeletePressed = {
                    when (stage) {
                        0 -> if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(1)
                        1 -> if (newPin.isNotEmpty()) newPin = newPin.dropLast(1)
                        2 -> if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                    }
                    message = ""
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ChangePinField(label: String, pinLength: Int, isActive: Boolean) {
    val borderColor = if (isActive)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(6) { i ->
                val filled = i < pinLength

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary else Color.Transparent,
                            CircleShape
                        )
                        .border(1.5.dp, borderColor, CircleShape)
                )
                if (i < 5) Spacer(Modifier.width(12.dp))
            }
        }
    }
}

@Composable
private fun ChangePasswordPinPad(
    onDigitPressed: (String) -> Unit,
    onDeletePressed: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ChangePasswordPinRow(listOf("1","2","3"), onDigitPressed)
        ChangePasswordPinRow(listOf("4","5","6"), onDigitPressed)
        ChangePasswordPinRow(listOf("7","8","9"), onDigitPressed)

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Spacer(Modifier.size(85.dp))

            ChangePasswordPinButton("0") { onDigitPressed("0") }

            Button(
                onClick = onDeletePressed,
                modifier = Modifier.size(85.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("⌫", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun ChangePasswordPinRow(
    digits: List<String>,
    onDigitPressed: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        digits.forEach {
            ChangePasswordPinButton(it) { onDigitPressed(it) }
        }
    }
}

@Composable
private fun ChangePasswordPinButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(85.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text, color = MaterialTheme.colorScheme.primary, fontSize = 24.sp)
    }
}