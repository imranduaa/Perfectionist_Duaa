package com.example.theperfectionist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostureHistoryScrn(navController: NavController) {
    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }
    var showDemo by remember { mutableStateOf(false) }

    val analysis by produceState<PostureAnalysisResult?>(
        initialValue = null,
        key1 = refreshKey,
        key2 = showDemo
    ) {
        value = withContext(Dispatchers.IO) {
            val savedTargetDays = TargetPrefs.getTargetDays(context)

            val samples = if (showDemo) {
                PostureAnalytics.loadDemoCsv(context)
            } else {
                loadCurrentUserSamples(context)
            }

            PostureAnalytics.analyze(
                samples = samples,
                targetDays = savedTargetDays
            )
        }
    }

    val result = analysis
    val savedTargetDays = TargetPrefs.getTargetDays(context)

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
                        text = "Posture History",
                        color = Color(0xFF003366),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF003366)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFA2CCFF).copy(alpha = 0.85f),
                    navigationIconContentColor = Color(0xFF003366),
                    titleContentColor = Color(0xFF003366)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showDemo = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showDemo) Color(0xFF005BBB) else Color.White.copy(alpha = 0.85f)
                        )
                    ) {
                        Text(
                            text = "Current User",
                            color = if (!showDemo) Color.White else Color(0xFF003366)
                        )
                    }

                    Button(
                        onClick = { showDemo = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showDemo) Color(0xFF005BBB) else Color.White.copy(alpha = 0.85f)
                        )
                    ) {
                        Text(
                            text = "Demo 7-Day",
                            color = if (showDemo) Color.White else Color(0xFF003366)
                        )
                    }
                }

                InfoBanner(
                    text = if (showDemo) {
                        "Showing demo history built from a 7-day sample dataset."
                    } else {
                        "This is a fresh user journey. The more you wear the device, the smarter your trend becomes."
                    }
                )

                if (result == null) {
                    StatsCard(
                        title = "Loading",
                        lines = listOf(
                            if (showDemo)
                                "Reading demo CSV and building predictions..."
                            else
                                "Preparing your posture journey..."
                        )
                    )
                } else {
                    val hasGraphData = result.graphPoints.isNotEmpty()

                    val summaryTitle = if (showDemo) {
                        "Demo 7-Day Posture Summary"
                    } else {
                        "Current User Posture Journey"
                    }

                    val summaryLines = if (showDemo) {
                        listOf(
                            "Days in graph: ${result.graphPoints.size}",
                            "Target goal: $savedTargetDays days",
                            result.targetProbabilityMessage,
                            result.bestPostureTimeMessage,
                            result.weakPostureTimeMessage,
                            result.guidanceMessage
                        )
                    } else {
                        if (hasGraphData) {
                            listOf(
                                "Days in graph: ${result.graphPoints.size}",
                                "Target goal: $savedTargetDays days",
                                result.targetProbabilityMessage,
                                result.bestPostureTimeMessage,
                                result.weakPostureTimeMessage,
                                result.guidanceMessage
                            )
                        } else {
                            listOf(
                                "Target goal: $savedTargetDays days",
                                "✨ You’ve started your posture journey.",
                                "📈 Your personal prediction begins after about 3 days of data.",
                                "🧠 By day 7, your posture trend becomes much smarter.",
                                "🎯 Keep wearing the device daily to unlock deeper insights."
                            )
                        }
                    }

                    StatsCard(
                        title = summaryTitle,
                        lines = summaryLines
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.94f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = if (showDemo)
                                    "Ideal posture trend (demo 7-day history)"
                                else
                                    "Ideal posture trend (current user)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF003366)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (result.graphPoints.isEmpty()) {
                                EmptyLearningChart(
                                    headline = if (showDemo)
                                        "No demo graph data found"
                                    else
                                        "Your graph is getting ready ✨",
                                    subtext = if (showDemo)
                                        "The demo history file is empty."
                                    else
                                        "Wear the device for a few days and your posture trend will begin to appear here."
                                )
                            } else {
                                SevenDayTrendChart(points = result.graphPoints)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (showDemo)
                                    "Higher percentages mean more IDEAL posture samples in the demo dataset."
                                else
                                    "This area will grow into your personal posture story as more data is collected.",
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Button(
                    onClick = { refreshKey++ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF03DAC5).copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        text = "Refresh",
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

private fun loadCurrentUserSamples(context: android.content.Context): List<PostureSample> {
    val file = File(context.filesDir, "posture_samples.csv")
    if (!file.exists()) return emptyList()

    return file.readLines()
        .drop(1)
        .mapNotNull { line -> parseStoredSampleLine(line) }
}

private fun parseStoredSampleLine(line: String): PostureSample? {
    if (line.isBlank()) return null

    val parts = line.split(",").map { it.trim() }
    if (parts.size < 4) return null

    val timestamp = parts.getOrNull(0)?.toLongOrNull() ?: return null
    val roll = parts.getOrNull(1)?.toFloatOrNull() ?: return null
    val pitch = parts.getOrNull(2)?.toFloatOrNull() ?: return null

    return when {
        parts.size >= 5 -> {
            val fourth = parts[3]
            val fifth = parts[4]

            if (fifth.equals("IDEAL", ignoreCase = true) ||
                fifth.equals("NORMAL", ignoreCase = true)
            ) {
                PostureSample(
                    timestamp = timestamp,
                    roll = roll,
                    pitch = pitch,
                    family = fourth,
                    postureState = fifth
                )
            } else if (fourth.equals("IDEAL", ignoreCase = true) ||
                fourth.equals("NORMAL", ignoreCase = true)
            ) {
                PostureSample(
                    timestamp = timestamp,
                    roll = roll,
                    pitch = pitch,
                    family = "UNKNOWN",
                    postureState = fourth
                )
            } else {
                PostureSample(
                    timestamp = timestamp,
                    roll = roll,
                    pitch = pitch,
                    family = fourth,
                    postureState = "NORMAL"
                )
            }
        }

        else -> {
            val posture = parts[3]
            PostureSample(
                timestamp = timestamp,
                roll = roll,
                pitch = pitch,
                family = "UNKNOWN",
                postureState = posture
            )
        }
    }
}

@Composable
private fun InfoBanner(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.88f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            color = Color(0xFF003366),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatsCard(title: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF003366)
            )

            Spacer(modifier = Modifier.height(8.dp))

            lines.forEach { line ->
                Text(
                    text = line,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun EmptyLearningChart(headline: String, subtext: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFF4F6F8), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = headline,
                color = Color(0xFF003366),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtext,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SevenDayTrendChart(points: List<DailyTrendPoint>) {
    val maxValue = max(points.maxOfOrNull { it.idealPercentage } ?: 100f, 100f)

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color(0xFFF4F6F8), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            val width = size.width
            val height = size.height
            val leftPad = 48f
            val bottomPad = 28f
            val topPad = 12f
            val usableWidth = width - leftPad - 12f
            val usableHeight = height - topPad - bottomPad

            drawLine(
                color = Color.Gray,
                start = Offset(leftPad, topPad),
                end = Offset(leftPad, topPad + usableHeight),
                strokeWidth = 3f
            )

            drawLine(
                color = Color.Gray,
                start = Offset(leftPad, topPad + usableHeight),
                end = Offset(leftPad + usableWidth, topPad + usableHeight),
                strokeWidth = 3f
            )

            repeat(5) { index ->
                val y = topPad + (usableHeight / 4f) * index
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.65f),
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + usableWidth, y),
                    strokeWidth = 1.5f
                )
            }

            val path = Path()

            points.forEachIndexed { index, point ->
                val xRatio =
                    if (points.size == 1) 0f
                    else index.toFloat() / (points.size - 1).toFloat()

                val yRatio = point.idealPercentage / maxValue

                val x = leftPad + usableWidth * xRatio
                val y = topPad + usableHeight - (usableHeight * yRatio)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                drawCircle(
                    color = Color(0xFF005BBB),
                    radius = 6f,
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = Color(0xFF005BBB),
                style = Stroke(width = 4f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { point ->
                Text(
                    text = point.dayLabel,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}