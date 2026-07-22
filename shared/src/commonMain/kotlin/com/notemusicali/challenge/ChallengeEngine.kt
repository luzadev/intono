package com.notemusicali.challenge

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max

@Serializable
data class ChallengeResult(
    val id: String,
    val dateMillis: Long,
    val sequenceName: String,
    val timeLimitSec: Int,
    val totalScore: Int,
    val notesPlayed: Int,
    val maxCombo: Int,
    val accuracy: Float,
)

enum class ChallengeTimeLimit(val seconds: Int, val displayName: String) {
    SHORT(30, "30s"),
    MEDIUM(60, "1 min"),
    LONG(120, "2 min"),
}

object ChallengeScoring {
    private const val BASE_POINTS = 100
    private const val PERFECT_BONUS = 50 // ≤5 cents
    private const val GOOD_BONUS = 25    // ≤15 cents
    private const val SPEED_BONUS = 30   // <2s reaction

    /**
     * Score a single note. Returns points earned.
     * @param centsDeviation absolute cents deviation
     * @param reactionTimeMs time to play the note
     * @param comboStreak current combo streak (0-based)
     * @param firstAttempt true if played correctly on first try
     */
    fun scoreNote(
        centsDeviation: Float,
        reactionTimeMs: Long,
        comboStreak: Int,
        firstAttempt: Boolean,
    ): Int {
        if (!firstAttempt) return 0

        val absCents = abs(centsDeviation)
        var points = BASE_POINTS

        // Accuracy bonus
        when {
            absCents <= 5f -> points += PERFECT_BONUS
            absCents <= 15f -> points += GOOD_BONUS
        }

        // Speed bonus
        if (reactionTimeMs < 2000) {
            points += SPEED_BONUS
        }

        // Combo multiplier: 1.0x base, +0.1x per streak note (max 2.0x)
        val multiplier = 1.0f + (comboStreak * 0.1f).coerceAtMost(1.0f)
        return (points * multiplier).toInt()
    }

    /**
     * Session accuracy: fraction of played notes that were correct on the first attempt.
     */
    fun accuracy(notesPlayed: Int, firstAttemptNotes: Int): Float =
        if (notesPlayed > 0) (firstAttemptNotes.toFloat() / notesPlayed).coerceAtMost(1f) else 0f
}
