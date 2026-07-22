package com.notemusicali.music

enum class ScaleType(val displayName: String, val intervals: List<Int>) {
    MAJOR("Maggiore", listOf(0, 2, 4, 5, 7, 9, 11, 12)),
    NATURAL_MINOR("Minore Naturale", listOf(0, 2, 3, 5, 7, 8, 10, 12)),
    HARMONIC_MINOR("Minore Armonica", listOf(0, 2, 3, 5, 7, 8, 11, 12)),
    MELODIC_MINOR("Minore Melodica", listOf(0, 2, 3, 5, 7, 9, 11, 12)),
    PENTATONIC_MAJOR("Pentatonica Maggiore", listOf(0, 2, 4, 7, 9, 12)),
    PENTATONIC_MINOR("Pentatonica Minore", listOf(0, 3, 5, 7, 10, 12)),
    CHROMATIC("Cromatica", listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)),
}

enum class ArpeggioType(val displayName: String, val intervals: List<Int>) {
    MAJOR("Maggiore", listOf(0, 4, 7, 12)),
    MINOR("Minore", listOf(0, 3, 7, 12)),
    DIMINISHED("Diminuito", listOf(0, 3, 6, 12)),
    AUGMENTED("Aumentato", listOf(0, 4, 8, 12)),
    DOMINANT_7TH("Settima Dom.", listOf(0, 4, 7, 10, 12)),
    MAJOR_7TH("Settima Magg.", listOf(0, 4, 7, 11, 12)),
    MINOR_7TH("Settima Min.", listOf(0, 3, 7, 10, 12)),
}

enum class ScaleDirection(val displayName: String) {
    ASCENDING("Ascendente"),
    DESCENDING("Discendente"),
    BOTH("Asc. + Disc."),
}

object ScaleGenerator {

    fun generateScale(
        rootNoteIndex: Int,
        octave: Int,
        scaleType: ScaleType,
        direction: ScaleDirection = ScaleDirection.ASCENDING,
    ): NoteSequence {
        val rootMidi = (octave + 1) * 12 + rootNoteIndex
        val rootName = ItalianNotation.fromNoteIndex(rootNoteIndex).display
        val name = "Scala $rootName ${scaleType.displayName}"

        val ascending = scaleType.intervals.map { interval ->
            MusicalNote.fromMidi(rootMidi + interval)
        }

        val notes = when (direction) {
            ScaleDirection.ASCENDING -> ascending
            ScaleDirection.DESCENDING -> ascending.reversed()
            ScaleDirection.BOTH -> ascending + ascending.dropLast(1).reversed()
        }

        return NoteSequence(name = name, notes = notes)
    }

    fun generateArpeggio(
        rootNoteIndex: Int,
        octave: Int,
        arpeggioType: ArpeggioType,
        direction: ScaleDirection = ScaleDirection.ASCENDING,
    ): NoteSequence {
        val rootMidi = (octave + 1) * 12 + rootNoteIndex
        val rootName = ItalianNotation.fromNoteIndex(rootNoteIndex).display
        val name = "Arpeggio $rootName ${arpeggioType.displayName}"

        val ascending = arpeggioType.intervals.map { interval ->
            MusicalNote.fromMidi(rootMidi + interval)
        }

        val notes = when (direction) {
            ScaleDirection.ASCENDING -> ascending
            ScaleDirection.DESCENDING -> ascending.reversed()
            ScaleDirection.BOTH -> ascending + ascending.dropLast(1).reversed()
        }

        return NoteSequence(name = name, notes = notes)
    }
}
