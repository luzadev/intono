package com.notemusicali.history

import kotlinx.serialization.Serializable

@Serializable
data class NoteResult(
    val noteName: String,
    val midiNumber: Int,
    val centsDeviation: Float,
    val reactionTimeMs: Long,
    val firstAttemptSuccess: Boolean,
)

@Serializable
data class PracticeSession(
    val id: String,
    val sequenceName: String,
    val instrumentName: String,
    val dateMillis: Long,
    val totalTimeMs: Long,
    val noteResults: List<NoteResult>,
    val noteMidiNumbers: List<Int>,
    val completed: Boolean = true,
    val beats: Int = 4,
    val beatType: Int = 4,
) {
    val averageCents: Float
        get() = if (noteResults.isEmpty()) 0f
        else noteResults.map { kotlin.math.abs(it.centsDeviation) }.average().toFloat()

    val perfectNotes: Int
        get() = noteResults.count { kotlin.math.abs(it.centsDeviation) <= 10f }

    val firstAttemptRate: Float
        get() = if (noteResults.isEmpty()) 0f
        else noteResults.count { it.firstAttemptSuccess }.toFloat() / noteResults.size
}
