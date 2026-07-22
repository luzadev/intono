package com.notemusicali.history

import com.notemusicali.scan.AppSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SessionRepository {
    private const val KEY = "practice_sessions"
    private const val MAX_SESSIONS = 50

    private val json = Json { ignoreUnknownKeys = true }

    fun load(): List<PracticeSession> {
        val raw = AppSettings.getString(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<PracticeSession>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(session: PracticeSession) {
        val current = load().toMutableList()
        current.add(0, session)
        if (current.size > MAX_SESSIONS) {
            current.subList(MAX_SESSIONS, current.size).clear()
        }
        AppSettings.putString(KEY, json.encodeToString(current))
    }
}
