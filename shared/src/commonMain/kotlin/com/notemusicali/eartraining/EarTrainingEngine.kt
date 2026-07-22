package com.notemusicali.eartraining

import com.notemusicali.audio.NoteConverter
import com.notemusicali.music.ItalianNotation
import kotlin.random.Random

enum class IntervalType(val semitones: Int, val displayName: String) {
    UNISON(0, "Unisono"),
    MINOR_2ND(1, "Seconda min."),
    MAJOR_2ND(2, "Seconda magg."),
    MINOR_3RD(3, "Terza min."),
    MAJOR_3RD(4, "Terza magg."),
    PERFECT_4TH(5, "Quarta giusta"),
    TRITONE(6, "Tritono"),
    PERFECT_5TH(7, "Quinta giusta"),
    MINOR_6TH(8, "Sesta min."),
    MAJOR_6TH(9, "Sesta magg."),
    MINOR_7TH(10, "Settima min."),
    MAJOR_7TH(11, "Settima magg."),
    OCTAVE(12, "Ottava"),
}

enum class EarTrainingMode(val displayName: String) {
    NOTE("Riconosci Nota"),
    INTERVAL("Riconosci Intervallo"),
}

enum class EarTrainingDifficulty(val displayName: String) {
    EASY("Facile"),
    MEDIUM("Medio"),
    HARD("Difficile"),
}

data class EarTrainingQuestion(
    val mode: EarTrainingMode,
    val referenceMidi: Int,
    val targetMidi: Int,
    val correctAnswer: String,
    val choices: List<String>,
    val interval: IntervalType? = null,
) {
    val referenceFrequency: Float get() = NoteConverter.midiToFrequency(referenceMidi)
    val targetFrequency: Float get() = NoteConverter.midiToFrequency(targetMidi)
}

object EarTrainingEngine {

    private val NOTE_NAMES = (0..11).map { ItalianNotation.fromNoteIndex(it).display }

    fun generateNoteQuestion(difficulty: EarTrainingDifficulty): EarTrainingQuestion {
        val midiRange = when (difficulty) {
            EarTrainingDifficulty.EASY -> 60..72    // C4-C5 (one octave)
            EarTrainingDifficulty.MEDIUM -> 55..79  // G3-G5
            EarTrainingDifficulty.HARD -> 48..84    // C3-C6
        }

        val targetMidi = Random.nextInt(midiRange.first, midiRange.last + 1)
        val targetNoteIndex = ((targetMidi % 12) + 12) % 12
        val correctName = NOTE_NAMES[targetNoteIndex]

        // Generate 3 wrong choices
        val wrongChoices = mutableSetOf<String>()
        while (wrongChoices.size < 3) {
            val wrong = NOTE_NAMES[Random.nextInt(12)]
            if (wrong != correctName) wrongChoices.add(wrong)
        }

        val choices = (wrongChoices.toList() + correctName).shuffled()

        return EarTrainingQuestion(
            mode = EarTrainingMode.NOTE,
            referenceMidi = targetMidi,
            targetMidi = targetMidi,
            correctAnswer = correctName,
            choices = choices,
        )
    }

    fun generateIntervalQuestion(difficulty: EarTrainingDifficulty): EarTrainingQuestion {
        val availableIntervals = when (difficulty) {
            EarTrainingDifficulty.EASY -> listOf(
                IntervalType.UNISON, IntervalType.MAJOR_3RD,
                IntervalType.PERFECT_5TH, IntervalType.OCTAVE,
            )
            EarTrainingDifficulty.MEDIUM -> listOf(
                IntervalType.MAJOR_2ND, IntervalType.MINOR_3RD, IntervalType.MAJOR_3RD,
                IntervalType.PERFECT_4TH, IntervalType.PERFECT_5TH,
                IntervalType.MINOR_7TH, IntervalType.OCTAVE,
            )
            EarTrainingDifficulty.HARD -> IntervalType.entries
        }

        val interval = availableIntervals.random()
        val baseMidi = Random.nextInt(60, 73) // C4-C5
        val targetMidi = baseMidi + interval.semitones

        // Generate wrong choices
        val wrongIntervals = availableIntervals.filter { it != interval }.shuffled().take(3)

        val choices = (wrongIntervals.map { it.displayName } + interval.displayName).shuffled()

        return EarTrainingQuestion(
            mode = EarTrainingMode.INTERVAL,
            referenceMidi = baseMidi,
            targetMidi = targetMidi,
            correctAnswer = interval.displayName,
            choices = choices,
            interval = interval,
        )
    }
}
