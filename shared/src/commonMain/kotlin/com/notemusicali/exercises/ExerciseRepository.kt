package com.notemusicali.exercises

import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteDuration
import com.notemusicali.music.NoteSequence

data class Exercise(
    val id: Int,
    val title: String,
    val description: String,
    val level: Int,
    val sequence: NoteSequence,
)

object ExerciseRepository {

    fun getAllExercises(): List<NoteSequence> = all.map { it.sequence }

    val all: List<Exercise> = listOf(
        Exercise(
            id = 1,
            title = "Prime Note",
            description = "5 note naturali base",
            level = 1,
            sequence = NoteSequence(
                name = "Prime Note",
                notes = listOf(
                    MusicalNote.fromNameAndOctave(0, 4),  // DO4
                    MusicalNote.fromNameAndOctave(2, 4),  // RE4
                    MusicalNote.fromNameAndOctave(4, 4),  // MI4
                    MusicalNote.fromNameAndOctave(5, 4),  // FA4
                    MusicalNote.fromNameAndOctave(7, 4),  // SOL4
                ),
            ),
        ),
        Exercise(
            id = 2,
            title = "Scala di DO Maggiore",
            description = "Scala completa su un'ottava",
            level = 2,
            sequence = NoteSequence(
                name = "Scala di DO Maggiore",
                notes = listOf(
                    MusicalNote.fromNameAndOctave(0, 4),  // DO4
                    MusicalNote.fromNameAndOctave(2, 4),  // RE4
                    MusicalNote.fromNameAndOctave(4, 4),  // MI4
                    MusicalNote.fromNameAndOctave(5, 4),  // FA4
                    MusicalNote.fromNameAndOctave(7, 4),  // SOL4
                    MusicalNote.fromNameAndOctave(9, 4),  // LA4
                    MusicalNote.fromNameAndOctave(11, 4), // SI4
                    MusicalNote.fromNameAndOctave(0, 5),  // DO5
                ),
            ),
        ),
        Exercise(
            id = 3,
            title = "Scala di SOL Maggiore",
            description = "Introduce FA# e cambio ottava",
            level = 3,
            sequence = NoteSequence(
                name = "Scala di SOL Maggiore",
                notes = listOf(
                    MusicalNote.fromNameAndOctave(7, 3),  // SOL3
                    MusicalNote.fromNameAndOctave(9, 3),  // LA3
                    MusicalNote.fromNameAndOctave(11, 3), // SI3
                    MusicalNote.fromNameAndOctave(0, 4),  // DO4
                    MusicalNote.fromNameAndOctave(2, 4),  // RE4
                    MusicalNote.fromNameAndOctave(4, 4),  // MI4
                    MusicalNote.fromNameAndOctave(6, 4),  // FA#4
                    MusicalNote.fromNameAndOctave(7, 4),  // SOL4
                ),
            ),
        ),
        Exercise(
            id = 4,
            title = "Arpeggi Maggiori",
            description = "Salti di terza e quinta",
            level = 4,
            sequence = NoteSequence(
                name = "Arpeggi Maggiori",
                notes = listOf(
                    MusicalNote.fromNameAndOctave(0, 4),  // DO4
                    MusicalNote.fromNameAndOctave(4, 4),  // MI4
                    MusicalNote.fromNameAndOctave(7, 4),  // SOL4
                    MusicalNote.fromNameAndOctave(0, 5),  // DO5
                    MusicalNote.fromNameAndOctave(7, 3),  // SOL3
                    MusicalNote.fromNameAndOctave(11, 3), // SI3
                    MusicalNote.fromNameAndOctave(2, 4),  // RE4
                    MusicalNote.fromNameAndOctave(7, 4),  // SOL4
                ),
            ),
        ),
        Exercise(
            id = 5,
            title = "Ave Maria — Schubert",
            description = "Melodia principale (Sib maggiore)",
            level = 3,
            sequence = NoteSequence(
                name = "Ave Maria — Schubert",
                notes = listOf(
                    MusicalNote.fromMidi(70),  // Sib4
                    MusicalNote.fromMidi(69),  // La4
                    MusicalNote.fromMidi(70),  // Sib4
                    MusicalNote.fromMidi(74),  // Re5
                    MusicalNote.fromMidi(72),  // Do5
                    MusicalNote.fromMidi(70),  // Sib4
                    MusicalNote.fromMidi(72),  // Do5
                    MusicalNote.fromMidi(74),  // Re5
                    MusicalNote.fromMidi(72),  // Do5
                    MusicalNote.fromMidi(70),  // Sib4
                    MusicalNote.fromMidi(69),  // La4
                    MusicalNote.fromMidi(67),  // Sol4
                    MusicalNote.fromMidi(69),  // La4
                    MusicalNote.fromMidi(70),  // Sib4
                    MusicalNote.fromMidi(74),  // Re5
                    MusicalNote.fromMidi(74),  // Re5
                    MusicalNote.fromMidi(72),  // Do5
                    MusicalNote.fromMidi(70),  // Sib4
                    MusicalNote.fromMidi(69),  // La4
                    MusicalNote.fromMidi(67),  // Sol4
                    MusicalNote.fromMidi(74),  // Re5
                    MusicalNote.fromMidi(76),  // Mi5
                    MusicalNote.fromMidi(74),  // Re5
                    MusicalNote.fromMidi(73),  // Do#5
                    MusicalNote.fromMidi(69),  // La4
                ),
            ),
        ),
        Exercise(
            id = 6,
            title = "Studio op.84 n.10 — Dancla",
            description = "Studio per violino in Sol maggiore",
            level = 4,
            sequence = NoteSequence(
                name = "Studio op.84 n.10 — Dancla",
                notes = listOf(
                    MusicalNote.fromMidi(55, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(59, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.HALF),
                    MusicalNote.fromMidi(59, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.HALF),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(72, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(76, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.HALF),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(79, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(83, NoteDuration.HALF),
                    MusicalNote.fromMidi(55, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(59, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.HALF),
                    MusicalNote.fromMidi(59, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.HALF),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(72, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(76, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.HALF),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(79, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.HALF),
                    MusicalNote.fromMidi(66, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(78, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(81, NoteDuration.HALF),
                    MusicalNote.fromMidi(81, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(79, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(76, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(73, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.HALF),
                    MusicalNote.fromMidi(64, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(73, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(76, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(81, NoteDuration.HALF),
                    MusicalNote.fromMidi(81, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(78, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(66, NoteDuration.HALF),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(76, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(79, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(83, NoteDuration.HALF),
                    MusicalNote.fromMidi(66, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(78, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(81, NoteDuration.HALF),
                    MusicalNote.fromMidi(81, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(79, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(76, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(73, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.HALF),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(66, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.HALF),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(66, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.HALF),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(70, NoteDuration.EIGHTH, preferFlat = true),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.HALF),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(70, NoteDuration.EIGHTH, preferFlat = true),
                    MusicalNote.fromMidi(70, NoteDuration.EIGHTH, preferFlat = true),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(79, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(79, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(78, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(78, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(81, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(81, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(86, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.HALF),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(66, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.HALF),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(70, NoteDuration.EIGHTH, preferFlat = true),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(70, NoteDuration.EIGHTH, preferFlat = true),
                    MusicalNote.fromMidi(62, NoteDuration.HALF),
                    MusicalNote.fromMidi(55, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(58, NoteDuration.EIGHTH, preferFlat = true),
                    MusicalNote.fromMidi(58, NoteDuration.EIGHTH, preferFlat = true),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(66, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(66, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(79, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(83, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(86, NoteDuration.HALF),
                    MusicalNote.fromMidi(86, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(84, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(81, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(78, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.HALF),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(78, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(81, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(86, NoteDuration.HALF),
                    MusicalNote.fromMidi(83, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(79, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(67, NoteDuration.HALF),
                    MusicalNote.fromMidi(60, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(64, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(72, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(76, NoteDuration.HALF),
                    MusicalNote.fromMidi(59, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(71, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.HALF),
                    MusicalNote.fromMidi(57, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(66, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(69, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(74, NoteDuration.HALF),
                    MusicalNote.fromMidi(67, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(59, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(62, NoteDuration.EIGHTH),
                    MusicalNote.fromMidi(55, NoteDuration.QUARTER),
                ),
                beats = 4,
                beatType = 4,
            ),
        ),
    )
}
