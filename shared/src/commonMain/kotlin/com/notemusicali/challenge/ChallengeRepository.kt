package com.notemusicali.challenge

import com.notemusicali.scan.AppSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ChallengeRepository {
    private const val KEY = "challenge_results"
    private const val MAX_RESULTS = 20

    private val json = Json { ignoreUnknownKeys = true }

    fun load(): List<ChallengeResult> {
        val raw = AppSettings.getString(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<ChallengeResult>>(raw)
        } catch (_: Exception) { emptyList() }
    }

    fun save(result: ChallengeResult) {
        val current = load().toMutableList()
        current.add(result)
        current.sortByDescending { it.totalScore }
        if (current.size > MAX_RESULTS) {
            current.subList(MAX_RESULTS, current.size).clear()
        }
        AppSettings.putString(KEY, json.encodeToString(current))
    }
}
