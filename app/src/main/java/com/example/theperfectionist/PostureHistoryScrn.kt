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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostureHistoryScrn(navController: NavController) {
    val context = LocalContext.current
    val storage = remember { PostureStorage(context) }
    var deleteDialogOpen by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }

    val samples by produceState(initialValue = emptyList<PostureSample>(), refreshKey) {
        value = withContext(Dispatchers.IO) { storage.readTodaySamples() }
    }

    val sampleCount by produceState(initialValue = 0, refreshKey) {
        value = withContext(Dispatchers.IO) { storage.sampleCount() }
    }

    val latestSaved by produceState(initialValue = "No saved data yet", refreshKey) {
        value = withContext(Dispatchers.IO) { storage.latestSavedTimeText() }
    }

    val averageScore = if (samples.isNotEmpty()) {
        samples.map { it.score }.average().toFloat()
    } else {
        0f
    }

    val worstScore = samples.maxOfOrNull { it.score } ?: 0f
    val postureSummary = when {
        worstScore >= 15f -> "Mostly poor posture"
        worstScore >= 8f -> "Mixed posture"
        samples.isNotEmpty() -> "Mostly good posture"
        else -> "No data yet"
    }

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
                StatsCard(
                    title = "Today",
                    lines = listOf(
                        "Samples shown: ${samples.size}",
                        "All saved samples: $sampleCount",
                        "Average score: ${"%.2f".format(averageScore)}",
                        "Worst score: ${"%.2f".format(worstScore)}",
                        "Summary: $postureSummary",
                        "Last saved: $latestSaved"
                    )
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Deviation graph for today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF003366)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (samples.size < 2) {
                            Text(
                                text = "Start receiving BLE data and leave the app running for a bit. Once at least 2 saved samples exist, the line graph will appear here.",
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            EmptyChart()
                        } else {
                            PostureLineChart(samples = samples)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Lower values mean posture closer to the baseline. Higher values mean more deviation from ideal posture.",
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { refreshKey++ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5).copy(alpha = 0.35f))
                    ) {
                        Text("Refresh", color = Color.DarkGray)
                    }

                    OutlinedButton(
                        onClick = { deleteDialogOpen = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7A0000))
                    ) {
                        Text("Delete All Data")
                    }
                }
            }
        }
    }

    if (deleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { deleteDialogOpen = false },
            title = { Text("Delete posture data?") },
            text = { Text("This removes all saved posture samples from the phone.") },
            confirmButton = {
                Button(onClick = {
                    storage.clearAllSamples()
                    refreshKey++
                    deleteDialogOpen = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatsCard(title: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF003366)
            )
            Spacer(modifier = Modifier.height(8.dp))
            lines.forEach { line ->
                Text(text = line, color = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun EmptyChart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFF4F6F8), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No graph data yet", color = Color.Gray)
    }
}

@Composable
private fun PostureLineChart(samples: List<PostureSample>) {
    val minTimestamp = samples.first().timestamp.toFloat()
    val maxTimestamp = samples.last().timestamp.toFloat()
    val maxScore = max(samples.maxOf { it.score }, 1f)
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

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

            // axes
            drawLine(
                color = Color.Gray,
                start = Offset(leftPad, topPad),
                end = Offset(leftPad, topPad + usableHeight),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.Gray,
                start = Offset(leftPad, topPad + usableHeight),
                end = Offset(leftPad + usableWidth, topPad + usableHeight),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // guide lines
            repeat(4) { index ->
                val y = topPad + (usableHeight / 4f) * index
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.65f),
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + usableWidth, y),
                    strokeWidth = 1.5f
                )
            }

            val path = Path()
            samples.forEachIndexed { index, sample ->
                val xRatio = if (maxTimestamp == minTimestamp) {
                    0f
                } else {
                    (sample.timestamp - minTimestamp) / (maxTimestamp - minTimestamp)
                }
                val yRatio = sample.score / maxScore

                val x = leftPad + usableWidth * xRatio
                val y = topPad + usableHeight - (usableHeight * yRatio)

                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                drawCircle(
                    color = scoreColor(sample.score),
                    radius = 5f,
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(timeFormatter.format(Date(samples.first().timestamp)), color = Color.DarkGray)
            Text(timeFormatter.format(Date(samples.last().timestamp)), color = Color.DarkGray)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LegendChip(label = "Good", color = Color(0xFF2E7D32))
            LegendChip(label = "Watch", color = Color(0xFFF9A825))
            LegendChip(label = "Bad", color = Color(0xFFC62828))
        }
    }
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = Color.DarkGray)
    }
}

private fun scoreColor(score: Float): Color {
    return when {
        score < 5f -> Color(0xFF2E7D32)
        score < 10f -> Color(0xFFF9A825)
        else -> Color(0xFFC62828)
    }
}
