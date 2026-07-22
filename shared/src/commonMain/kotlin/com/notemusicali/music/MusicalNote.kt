package com.notemusicali.music

enum class NoteDuration(val holdMs: Long) {
    WHOLE(2500),
    HALF(1500),
    QUARTER(1000),
    EIGHTH(600),
    SIXTEENTH(400),
    THIRTY_SECOND(300),
    ;

    companion object {
        fun fromMusicXml(type: String): NoteDuration = when (type) {
            "whole" -> WHOLE
            "half" -> HALF
            "quarter" -> QUARTER
            "eighth" -> EIGHTH
            "16th" -> SIXTEENTH
            "32nd" -> THIRTY_SECOND
            else -> QUARTER
        }
    }
}

/**
 * Represents a musical note with its properties.
 */
data class MusicalNote(
    val noteIndex: Int,
    val octave: Int,
    val italianName: ItalianNotation.NoteName,
    val midiNumber: Int,
    val duration: NoteDuration = NoteDuration.QUARTER,
    // Enharmonic preference: a black key can be spelled as sharp (default) or flat
    val preferFlat: Boolean = false,
) {
    val displayName: String
        get() = if (preferFlat) italianName.flat ?: italianName.sharp else italianName.sharp
    val fullName: String get() = "$displayName$octave"

    companion object {
        fun fromMidi(
            midiNumber: Int,
            duration: NoteDuration = NoteDuration.QUARTER,
            preferFlat: Boolean = false,
        ): MusicalNote {
            val noteIndex = ((midiNumber % 12) + 12) % 12
            val octave = (midiNumber / 12) - 1
            return MusicalNote(
                noteIndex = noteIndex,
                octave = octave,
                italianName = ItalianNotation.fromNoteIndex(noteIndex),
                midiNumber = midiNumber,
                duration = duration,
                preferFlat = preferFlat,
            )
        }

        fun fromNameAndOctave(
            noteIndex: Int,
            octave: Int,
            duration: NoteDuration = NoteDuration.QUARTER,
            preferFlat: Boolean = false,
        ): MusicalNote {
            val midiNumber = (octave + 1) * 12 + noteIndex
            return MusicalNote(
                noteIndex = noteIndex,
                octave = octave,
                italianName = ItalianNotation.fromNoteIndex(noteIndex),
                midiNumber = midiNumber,
                duration = duration,
                preferFlat = preferFlat,
            )
        }
    }
}
