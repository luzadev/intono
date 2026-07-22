package com.notemusicali.exercises

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExerciseRepositoryTest {

    @Test
    fun `dancla study op84 n10 is available as exercise`() {
        val exercise = ExerciseRepository.all.firstOrNull { it.title.contains("Dancla") }
        assertNotNull(exercise, "lo studio di Dancla deve essere tra gli esercizi")
        assertTrue(exercise.sequence.notes.size > 100, "atteso lo studio completo, trovate ${exercise.sequence.notes.size} note")
        assertEquals(4, exercise.sequence.beats)
        assertEquals(4, exercise.sequence.beatType)
        // Studio per violino: tutte le note dentro l'estensione del violino (G3..E7)
        assertTrue(exercise.sequence.notes.all { it.midiNumber in 55..100 })
    }

    @Test
    fun `exercise ids are unique`() {
        val ids = ExerciseRepository.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
