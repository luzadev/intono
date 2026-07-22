package com.notemusicali.stats

import com.notemusicali.history.PracticeSession
import com.notemusicali.history.SessionRepository
import kotlin.math.abs

data class DailyStats(
    val dateLabel: String,
    val sessionCount: Int,
    val totalTimeMs: Long,
    val averageCents: Float,
    val firstAttemptRate: Float,
    val averageReactionMs: Long,
)

data class OverallStats(
    val totalSessions: Int,
    val totalPracticeTimeMs: Long,
    val bestAccuracyCents: Float,
    val averageCents: Float,
    val averageFirstAttemptRate: Float,
    val completedSessions: Int,
)

object StatsCalculator {

    fun calculateOverall(sessions: List<PracticeSession> = SessionRepository.load()): OverallStats {
        if (sessions.isEmpty()) return OverallStats(0, 0L, 0f, 0f, 0f, 0)

        val totalTime = sessions.sumOf { it.totalTimeMs }
        val withResults = sessions.filter { it.noteResults.isNotEmpty() }
        val avgCents = if (withResults.isEmpty()) 0f
            else withResults.map { it.averageCents }.average().toFloat()
        val bestCents = if (withResults.isEmpty()) 0f
            else withResults.minOf { it.averageCents }
        val avgFirstAttempt = if (withResults.isEmpty()) 0f
            else withResults.map { it.firstAttemptRate }.average().toFloat()

        return OverallStats(
            totalSessions = sessions.size,
            totalPracticeTimeMs = totalTime,
            bestAccuracyCents = bestCents,
            averageCents = avgCents,
            averageFirstAttemptRate = avgFirstAttempt,
            completedSessions = sessions.count { it.completed },
        )
    }

    fun calculateDaily(sessions: List<PracticeSession> = SessionRepository.load()): List<DailyStats> {
        if (sessions.isEmpty()) return emptyList()

        // Group by day (using dateMillis / MS_PER_DAY)
        val grouped = sessions.groupBy { it.dateMillis / 86_400_000L }

        return grouped.entries
            .sortedBy { it.key }
            .map { (dayKey, daySessions) ->
                val withResults = daySessions.filter { it.noteResults.isNotEmpty() }
                val avgCents = if (withResults.isEmpty()) 0f
                    else withResults.map { it.averageCents }.average().toFloat()
                val avgFirstAttempt = if (withResults.isEmpty()) 0f
                    else withResults.map { it.firstAttemptRate }.average().toFloat()
                val avgReaction = if (withResults.isEmpty()) 0L
                    else withResults.flatMap { it.noteResults }
                        .map { it.reactionTimeMs }
                        .average().toLong()

                // Simple date label from epoch day
                val totalDays = dayKey
                val year = estimateYear(totalDays)
                val dayOfYear = estimateDayOfYear(totalDays, year)
                val (month, day) = dayOfYearToMonthDay(dayOfYear, isLeapYear(year))
                val dateLabel = "${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}"

                DailyStats(
                    dateLabel = dateLabel,
                    sessionCount = daySessions.size,
                    totalTimeMs = daySessions.sumOf { it.totalTimeMs },
                    averageCents = avgCents,
                    firstAttemptRate = avgFirstAttempt,
                    averageReactionMs = avgReaction,
                )
            }
    }

    // Simple epoch day → year/month/day helpers (no platform dependency)
    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    private fun estimateYear(epochDay: Long): Int {
        var y = 1970
        var remaining = epochDay
        while (true) {
            val daysInYear = if (isLeapYear(y)) 366L else 365L
            if (remaining < daysInYear) return y
            remaining -= daysInYear
            y++
        }
    }

    private fun estimateDayOfYear(epochDay: Long, year: Int): Int {
        var remaining = epochDay
        var y = 1970
        while (y < year) {
            remaining -= if (isLeapYear(y)) 366L else 365L
            y++
        }
        return remaining.toInt() + 1
    }

    private fun dayOfYearToMonthDay(dayOfYear: Int, leap: Boolean): Pair<Int, Int> {
        val daysInMonths = if (leap) intArrayOf(31,29,31,30,31,30,31,31,30,31,30,31)
            else intArrayOf(31,28,31,30,31,30,31,31,30,31,30,31)
        var remaining = dayOfYear
        for (m in daysInMonths.indices) {
            if (remaining <= daysInMonths[m]) return Pair(m + 1, remaining)
            remaining -= daysInMonths[m]
        }
        return Pair(12, 31)
    }
}
