package com.notemusicali.scan

import com.notemusicali.music.ItalianNotation
import com.notemusicali.music.MusicalNote

object NoteParser {

    private val NOTE_REGEX = Regex("""^([A-Ga-g][#b]?)(\d)$""")

    fun parseNotes(content: String): List<MusicalNote> {
        return content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val match = NOTE_REGEX.matchEntire(line) ?: return@mapNotNull null
                val noteName = match.groupValues[1]
                val octave = match.groupValues[2].toIntOrNull() ?: return@mapNotNull null

                val step = noteName.first().uppercase()
                val alter = when {
                    noteName.length > 1 && noteName[1] == '#' -> 1
                    noteName.length > 1 && noteName[1] == 'b' -> -1
                    else -> 0
                }

                val noteIndex = ItalianNotation.fromMusicXmlStep(step, alter)
                    ?: return@mapNotNull null

                MusicalNote.fromNameAndOctave(noteIndex, octave, preferFlat = alter < 0)
            }
    }
}
