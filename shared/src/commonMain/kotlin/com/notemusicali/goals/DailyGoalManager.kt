package com.notemusicali.goals

import com.notemusicali.scan.AppSettings
import com.notemusicali.util.currentDateString
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class DailyGoalSettings(
    val targetMinutes: Int = 15,
)

@Serializable
data class DailyGoalState(
    val streak: Int = 0,
    val lastGoalMetDate: String = "",
    val todayMs: Long = 0L,
    val todayDate: String = "",
    val history: Map<String, Long> = emptyMap(), // date -> ms practiced
)

object DailyGoalManager {

    private const val SETTINGS_KEY = "daily_goal_settings"
    private const val STATE_KEY = "daily_goal_state"
    private val json = Json { ignoreUnknownKeys = true }

    fun getSettings(): DailyGoalSettings {
        val raw = AppSettings.getString(SETTINGS_KEY) ?: return DailyGoalSettings()
        return try {
            json.decodeFromString<DailyGoalSettings>(raw)
        } catch (_: Exception) { DailyGoalSettings() }
    }

    fun saveSettings(settings: DailyGoalSettings) {
        AppSettings.putString(SETTINGS_KEY, json.encodeToString(settings))
    }

    fun getState(): DailyGoalState {
        val raw = AppSettings.getString(STATE_KEY) ?: return DailyGoalState()
        return try {
            val state = json.decodeFromString<DailyGoalState>(raw)
            val today = currentDateString()
            if (state.todayDate != today) {
                // New day — reset today counter, keep history
                state.copy(todayMs = 0L, todayDate = today)
            } else state
        } catch (_: Exception) { DailyGoalState() }
    }

    fun recordPractice(durationMs: Long) {
        val today = currentDateString()
        val settings = getSettings()
        var state = getState()

        // Ensure todayDate is current
        if (state.todayDate != today) {
            state = state.copy(todayMs = 0L, todayDate = today)
        }

        val newTodayMs = state.todayMs + durationMs
        val updatedHistory = state.history.toMutableMap()
        updatedHistory[today] = newTodayMs

        // Keep only last 60 days of history
        val sortedKeys = updatedHistory.keys.sorted()
        if (sortedKeys.size > 60) {
            sortedKeys.take(sortedKeys.size - 60).forEach { updatedHistory.remove(it) }
        }

        val targetMs = settings.targetMinutes * 60_000L
        val goalMetToday = newTodayMs >= targetMs
        val wasMetBefore = state.todayMs >= targetMs

        var streak = state.streak
        if (goalMetToday && !wasMetBefore) {
            // Calculate streak: check if yesterday was the last goal-met date
            val yesterday = previousDateString(today)
            streak = if (state.lastGoalMetDate == yesterday || state.lastGoalMetDate == today) {
                state.streak + 1
            } else {
                1
            }
        }

        val newState = state.copy(
            todayMs = newTodayMs,
            todayDate = today,
            streak = streak,
            lastGoalMetDate = if (goalMetToday) today else state.lastGoalMetDate,
            history = updatedHistory,
        )

        AppSettings.putString(STATE_KEY, json.encodeToString(newState))
    }

    fun isTodayGoalMet(): Boolean {
        val settings = getSettings()
        val state = getState()
        val today = currentDateString()
        if (state.todayDate != today) return false
        return state.todayMs >= settings.targetMinutes * 60_000L
    }

    fun todayProgressFraction(): Float {
        val settings = getSettings()
        val state = getState()
        val today = currentDateString()
        if (state.todayDate != today) return 0f
        val target = settings.targetMinutes * 60_000L
        if (target <= 0) return 1f
        return (state.todayMs.toFloat() / target).coerceAtMost(1f)
    }

    /** Get last N days of practice history (date -> minutes). */
    fun recentHistory(days: Int = 30): List<Pair<String, Int>> {
        val state = getState()
        val allDates = state.history.entries
            .sortedByDescending { it.key }
            .take(days)
            .reversed()
        return allDates.map { (date, ms) -> date to (ms / 60_000L).toInt() }
    }

    private fun previousDateString(yyyymmdd: String): String {
        if (yyyymmdd.length != 8) return ""
        val y = yyyymmdd.substring(0, 4).toIntOrNull() ?: return ""
        val m = yyyymmdd.substring(4, 6).toIntOrNull() ?: return ""
        val d = yyyymmdd.substring(6, 8).toIntOrNull() ?: return ""

        return if (d > 1) {
            "$y${m.toString().padStart(2, '0')}${(d - 1).toString().padStart(2, '0')}"
        } else if (m > 1) {
            val prevMonth = m - 1
            val daysInPrevMonth = daysInMonth(prevMonth, y)
            "$y${prevMonth.toString().padStart(2, '0')}${daysInPrevMonth.toString().padStart(2, '0')}"
        } else {
            "${y - 1}1231"
        }
    }

    private fun daysInMonth(month: Int, year: Int): Int = when (month) {
        1 -> 31; 2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        3 -> 31; 4 -> 30; 5 -> 31; 6 -> 30; 7 -> 31; 8 -> 31
        9 -> 30; 10 -> 31; 11 -> 30; 12 -> 31; else -> 30
    }
}
