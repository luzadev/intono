package com.notemusicali.eartraining

import com.notemusicali.scan.AppSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class EarTrainingSession(
    val id: String,
    val dateMillis: Long,
    val mode: String,
    val difficulty: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
) {
    val accuracy: Float get() = if (totalQuestions == 0) 0f
        else correctAnswers.toFloat() / totalQuestions
}

object EarTrainingStatsRepository {
    private const val KEY = "ear_training_sessions"
    private const val MAX_SESSIONS = 50

    private val json = Json { ignoreUnknownKeys = true }

    fun load(): List<EarTrainingSession> {
        val raw = AppSettings.getString(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<EarTrainingSession>>(raw)
        } catch (_: Exception) { emptyList() }
    }

    fun save(session: EarTrainingSession) {
        val current = load().toMutableList()
        current.add(0, session)
        if (current.size > MAX_SESSIONS) {
            current.subList(MAX_SESSIONS, current.size).clear()
        }
        AppSettings.putString(KEY, json.encodeToString(current))
    }
}
