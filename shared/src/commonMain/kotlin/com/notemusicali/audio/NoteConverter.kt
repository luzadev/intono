package com.notemusicali.audio

import com.notemusicali.music.ItalianNotation
import com.notemusicali.music.MusicalNote
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Converts frequency (Hz) to musical note information.
 * Uses A4 = 440Hz as reference pitch.
 */
object NoteConverter {

    private const val A4_FREQUENCY = 440.0
    private const val A4_MIDI = 69

    data class NoteResult(
        val note: MusicalNote,
        val centsDeviation: Float,
        val frequency: Float,
    )

    fun frequencyToNote(frequency: Float): NoteResult {
        val midiNumber = frequencyToMidi(frequency)
        val roundedMidi = midiNumber.roundToInt()
        val centsDeviation = ((midiNumber - roundedMidi) * 100).toFloat()

        val noteIndex = ((roundedMidi % 12) + 12) % 12
        val octave = (roundedMidi / 12) - 1

        val italianName = ItalianNotation.fromNoteIndex(noteIndex)

        val note = MusicalNote(
            noteIndex = noteIndex,
            octave = octave,
            italianName = italianName,
            midiNumber = roundedMidi,
        )

        return NoteResult(
            note = note,
            centsDeviation = centsDeviation,
            frequency = frequency,
        )
    }

    private fun frequencyToMidi(frequency: Float): Double {
        return A4_MIDI + 12.0 * log2(frequency.toDouble() / A4_FREQUENCY)
    }

    fun midiToFrequency(midiNumber: Int): Float {
        return (A4_FREQUENCY * 2.0.pow((midiNumber - A4_MIDI).toDouble() / 12.0)).toFloat()
    }
}
