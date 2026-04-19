package com.example.theperfectionist

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class DailyTrendPoint(
    val dayLabel: String,
    val idealPercentage: Float,
    val totalSamples: Int,
    val normalSamples: Int,
    val dayStartMillis: Long
)

data class PostureAnalysisResult(
    val graphPoints: List<DailyTrendPoint>,
    val targetProbabilityMessage: String,
    val bestPostureTimeMessage: String,
    val weakPostureTimeMessage: String,
    val guidanceMessage: String
)

object PostureAnalytics {

    fun loadDemoCsv(
        context: Context,
        fileName: String = "posture_demo.csv"
    ): List<PostureSample> {
        return context.assets.open(fileName).bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null

                val parts = line.split(",")
                if (parts.size < 5) return@mapNotNull null

                val timestamp = parts[0].trim().toLongOrNull() ?: return@mapNotNull null
                val roll = parts[1].trim().toFloatOrNull() ?: return@mapNotNull null
                val pitch = parts[2].trim().toFloatOrNull() ?: return@mapNotNull null
                val family = parts[3].trim()
                val postureState = parts[4].trim()

                PostureSample(
                    timestamp = timestamp,
                    roll = roll,
                    pitch = pitch,
                    family = family,
                    postureState = postureState
                )
            }.toList()
        }
    }

    fun analyze(
        samples: List<PostureSample>,
        targetDays: Int = 14
    ): PostureAnalysisResult {
        val graphPoints = buildRollingLast7Days(samples)

        return PostureAnalysisResult(
            graphPoints = graphPoints,
            targetProbabilityMessage = estimateTargetProbability(graphPoints, targetDays),
            bestPostureTimeMessage = detectBestPostureTime(samples),
            weakPostureTimeMessage = detectWeakPostureTime(samples),
            guidanceMessage = buildGuidanceMessage(graphPoints)
        )
    }

    fun buildRollingLast7Days(samples: List<PostureSample>): List<DailyTrendPoint> {
        if (samples.isEmpty()) return emptyList()

        val sorted = samples.sortedBy { it.timestamp }

        val grouped = sorted.groupBy { sample ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = sample.timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSortedMap()

        val last7 = grouped.entries.toList().takeLast(7)
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())

        return last7.map { entry ->
            val dayStart = entry.key
            val daySamples = entry.value

            val total = daySamples.size
            val normal = daySamples.count {
                it.postureState.equals("NORMAL", ignoreCase = true)
            }
            val ideal = daySamples.count {
                it.postureState.equals("IDEAL", ignoreCase = true)
            }

            val idealPercentage = if (total == 0) 0f else (ideal * 100f) / total.toFloat()

            DailyTrendPoint(
                dayLabel = formatter.format(Date(dayStart)),
                idealPercentage = idealPercentage,
                totalSamples = total,
                normalSamples = normal,
                dayStartMillis = dayStart
            )
        }
    }

    fun estimateTargetProbability(
        points: List<DailyTrendPoint>,
        targetDays: Int
    ): String {
        val daysAvailable = points.size

        if (daysAvailable == 0) {
            return "🎯 Start wearing the device daily to unlock your target prediction."
        }

        if (daysAvailable < 3) {
            return "🎯 Target prediction begins after about 3 days of data."
        }

        val percentages = points.map { it.idealPercentage }
        val currentScore = percentages.last()
        val averageScore = percentages.average().toFloat()

        val slope = calculateSlope(percentages)
        val consistencyPenalty = calculateConsistencyPenalty(percentages)
        val recentMomentumBonus = calculateMomentumBonus(percentages)

        val projectedScore = (currentScore + slope * targetDays).coerceIn(0f, 100f)

        // Human-style scoring:
        // - current posture matters most
        // - projected trend matters a lot
        // - consistency affects confidence
        // - recent momentum gives a small bonus
        val rawProbability =
            (averageScore * 0.30f) +
                    (currentScore * 0.25f) +
                    (projectedScore * 0.30f) +
                    recentMomentumBonus -
                    consistencyPenalty

        val probabilityPercent = rawProbability
            .coerceIn(18f, 92f)
            .toInt()

        return if (daysAvailable < 7) {
            "⚠️ Early estimate: you currently have about a $probabilityPercent% chance of reaching your posture target in $targetDays days."
        } else {
            when {
                probabilityPercent >= 80 ->
                    "🎉 Strong progress: you currently have about a $probabilityPercent% chance of reaching your posture target in $targetDays days."
                probabilityPercent >= 60 ->
                    "✅ Good progress: you currently have about a $probabilityPercent% chance of reaching your posture target in $targetDays days."
                probabilityPercent >= 40 ->
                    "📈 You are improving, with about a $probabilityPercent% chance of reaching your posture target in $targetDays days."
                else ->
                    "⚠️ Right now the trend is weaker, with about a $probabilityPercent% chance of reaching your posture target in $targetDays days."
            }
        }
    }

    private fun calculateSlope(percentages: List<Float>): Float {
        if (percentages.size < 2) return 0f

        val x = percentages.indices.map { it.toFloat() }
        val y = percentages

        val xMean = x.average().toFloat()
        val yMean = y.average().toFloat()

        var numerator = 0f
        var denominator = 0f

        for (i in x.indices) {
            numerator += (x[i] - xMean) * (y[i] - yMean)
            denominator += (x[i] - xMean) * (x[i] - xMean)
        }

        return if (denominator == 0f) 0f else numerator / denominator
    }

    private fun calculateConsistencyPenalty(percentages: List<Float>): Float {
        if (percentages.size < 2) return 0f

        var totalJump = 0f
        for (i in 1 until percentages.size) {
            totalJump += abs(percentages[i] - percentages[i - 1])
        }

        val averageJump = totalJump / (percentages.size - 1).toFloat()

        return when {
            averageJump >= 30f -> 18f
            averageJump >= 20f -> 12f
            averageJump >= 12f -> 7f
            averageJump >= 6f -> 3f
            else -> 0f
        }
    }

    private fun calculateMomentumBonus(percentages: List<Float>): Float {
        if (percentages.size < 3) return 0f

        val last = percentages.last()
        val previous = percentages[percentages.size - 2]
        val thirdLast = percentages[percentages.size - 3]

        val recentLift = (last - previous) + ((previous - thirdLast) * 0.5f)

        return when {
            recentLift >= 20f -> 8f
            recentLift >= 10f -> 5f
            recentLift >= 4f -> 2f
            recentLift <= -20f -> -8f
            recentLift <= -10f -> -5f
            recentLift <= -4f -> -2f
            else -> 0f
        }
    }

    fun detectBestPostureTime(samples: List<PostureSample>): String {
        val idealSamples = samples.filter {
            it.postureState.equals("IDEAL", ignoreCase = true)
        }

        if (idealSamples.isEmpty()) {
            return "🌟 Best-posture time will appear as more data is collected."
        }

        val hourCounts = IntArray(24)

        for (sample in idealSamples) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = sample.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hourCounts[hour]++
        }

        val peakHour = hourCounts.indices.maxByOrNull { hourCounts[it] } ?: 0
        val endHour = (peakHour + 2) % 24

        return "🌟 You tend to have your best posture around ${formatHour(peakHour)} - ${formatHour(endHour)}."
    }

    fun detectWeakPostureTime(samples: List<PostureSample>): String {
        val normalSamples = samples.filter {
            it.postureState.equals("NORMAL", ignoreCase = true)
        }

        if (normalSamples.isEmpty()) {
            return "⚠️ Weak-posture time will appear as more data is collected."
        }

        val hourCounts = IntArray(24)

        for (sample in normalSamples) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = sample.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hourCounts[hour]++
        }

        val peakHour = hourCounts.indices.maxByOrNull { hourCounts[it] } ?: 0
        val endHour = (peakHour + 2) % 24

        return "⚠️ You tend to have weaker posture around ${formatHour(peakHour)} - ${formatHour(endHour)}."
    }

    fun buildGuidanceMessage(points: List<DailyTrendPoint>): String {
        val daysAvailable = points.size

        if (daysAvailable == 0) {
            return "Start using the device to begin building your posture trend."
        }

        if (daysAvailable < 3) {
            return "We are still learning your posture habits. Keep using the device daily."
        }

        val percentages = points.map { it.idealPercentage }
        val slope = calculateSlope(percentages)

        return when {
            daysAvailable < 7 ->
                "Your posture pattern is still learning and becomes more reliable with more daily data."
            slope > 4f ->
                "You are showing a strong upward trend. Keep following the same routine."
            slope > 1f ->
                "Your posture is improving steadily. Daily consistency will help a lot."
            slope >= -1f ->
                "Your posture is fairly stable right now. Small daily improvements can push this higher."
            else ->
                "Your recent trend has dipped a little. Try paying extra attention during your weaker posture hours."
        }
    }

    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12 AM"
            hour < 12 -> "$hour AM"
            hour == 12 -> "12 PM"
            else -> "${hour - 12} PM"
        }
    }
}