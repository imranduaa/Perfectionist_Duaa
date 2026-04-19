package com.example.theperfectionist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetDateScrn(navController: NavController, mac: String) {
    val context = LocalContext.current

    var targetDays by remember { mutableStateOf("14") }
    var errorText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA2CCFF).copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Goal Setup",
                        color = Color(0xFF003366),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFA2CCFF).copy(alpha = 0.85f),
                    titleContentColor = Color(0xFF003366)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.96f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "You’re all set ✨",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF003366)
                        )

                        Text(
                            text = "Great start. Better posture is built day by day. Pick a goal window and let’s make this your next healthy habit.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )

                        Text(
                            text = "Quick goal picks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF003366)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GoalChip(
                                    label = "7 days",
                                    selected = targetDays == "7",
                                    onClick = {
                                        targetDays = "7"
                                        errorText = ""
                                    }
                                )
                                GoalChip(
                                    label = "14 days",
                                    selected = targetDays == "14",
                                    onClick = {
                                        targetDays = "14"
                                        errorText = ""
                                    }
                                )
                                GoalChip(
                                    label = "21 days",
                                    selected = targetDays == "21",
                                    onClick = {
                                        targetDays = "21"
                                        errorText = ""
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GoalChip(
                                    label = "30 days",
                                    selected = targetDays == "30",
                                    onClick = {
                                        targetDays = "30"
                                        errorText = ""
                                    }
                                )
                                GoalChip(
                                    label = "45 days",
                                    selected = targetDays == "45",
                                    onClick = {
                                        targetDays = "45"
                                        errorText = ""
                                    }
                                )
                                GoalChip(
                                    label = "60 days",
                                    selected = targetDays == "60",
                                    onClick = {
                                        targetDays = "60"
                                        errorText = ""
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Or choose your own number",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF003366)
                        )

                        OutlinedTextField(
                            value = targetDays,
                            onValueChange = {
                                targetDays = it.filter { ch -> ch.isDigit() }
                                errorText = ""
                            },
                            label = { Text("Target days") },
                            placeholder = { Text("Example: 14") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        val previewDays = targetDays.toIntOrNull()
                        if (previewDays != null && previewDays > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFEAF4FF)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Text(
                                        text = "Your posture mission",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF003366)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "We’ll use your posture data to estimate progress over the next $previewDays days.",
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        if (errorText.isNotEmpty()) {
                            Text(
                                text = errorText,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val days = targetDays.toIntOrNull()

                                if (days == null || days <= 0) {
                                    errorText = "Please enter a valid number of days."
                                } else {
                                    TargetPrefs.saveTargetDays(context, days)

                                    navController.navigate("calibration/$mac") {
                                        popUpTo("target_date/$mac") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF005BBB)
                            )
                        ) {
                            Text(
                                text = "Start My Journey",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "You can update this target later if your plan changes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = 1.5.dp,
                color = if (selected) Color(0xFF005BBB) else Color(0xFFB0C4DE),
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                color = if (selected) Color(0xFF005BBB).copy(alpha = 0.12f) else Color.White,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF005BBB) else Color.DarkGray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}