package com.example.theperfectionist

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PostureStorage(private val context: Context) {

    private val fileName = "posture_samples.csv"
    private val header = "timestamp,roll,pitch,family,postureState\n"

    private fun file(): File = File(context.filesDir, fileName)

    @Synchronized
    fun appendSample(sample: PostureSample) {
        val target = file()
        if (!target.exists()) {
            target.writeText(header)
        }

        val line = listOf(
            sample.timestamp,
            sample.roll,
            sample.pitch,
            sample.family,
            sample.postureState
        ).joinToString(",") + "\n"

        target.appendText(line)
    }

    fun readAllSamples(): List<PostureSample> {
        val target = file()
        if (!target.exists()) return emptyList()

        return target.readLines()
            .drop(1)
            .mapNotNull { parseLine(it) }
            .sortedBy { it.timestamp }
    }

    fun readTodaySamples(): List<PostureSample> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val end = calendar.timeInMillis

        return readAllSamples().filter { it.timestamp in start until end }
    }

    fun clearAllSamples() {
        val target = file()
        if (target.exists()) {
            target.delete()
        }
    }

    fun sampleCount(): Int = readAllSamples().size

    fun latestSavedTimeText(): String {
        val latest = readAllSamples().maxByOrNull { it.timestamp } ?: return "No saved data yet"
        val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        return formatter.format(Date(latest.timestamp))
    }

    private fun parseLine(line: String): PostureSample? {
        if (line.isBlank()) return null

        val parts = line.split(",")
        if (parts.size < 5) return null

        val timestamp = parts[0].trim().toLongOrNull() ?: return null
        val roll = parts[1].trim().toFloatOrNull() ?: return null
        val pitch = parts[2].trim().toFloatOrNull() ?: return null
        val family = parts[3].trim()
        val postureState = parts[4].trim()

        return PostureSample(
            timestamp = timestamp,
            roll = roll,
            pitch = pitch,
            family = family,
            postureState = postureState
        )
    }
}