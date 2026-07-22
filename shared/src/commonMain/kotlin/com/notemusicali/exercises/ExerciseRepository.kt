package com.notemusicali.exercises

import com.notemusicali.music.MusicalNote
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
    )
}
