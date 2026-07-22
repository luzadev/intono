package com.notemusicali.exercises

import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteDuration
import com.notemusicali.music.NoteSequence
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportedExerciseStoreTest {

    private fun sequence() = NoteSequence(
        name = "Test",
        notes = listOf(
            MusicalNote.fromMidi(60, NoteDuration.EIGHTH),
            MusicalNote.fromMidi(70, NoteDuration.HALF, preferFlat = true),
        ),
        beats = 3,
        beatType = 4,
    )

    @Test
    fun `added exercise round trips with notes durations flats and meter`() {
        val store = ImportedExerciseStore(MapSettings())
        store.add("Il mio spartito", sequence(), nowMs = 1000L)

        val loaded = store.getAll().single()
        assertEquals("Il mio spartito", loaded.title)
        val seq = loaded.toNoteSequence()
        assertEquals(2, seq.notes.size)
        assertEquals(60, seq.notes[0].midiNumber)
        assertEquals(NoteDuration.EIGHTH, seq.notes[0].duration)
        assertEquals("SIb", seq.notes[1].displayName)
        assertEquals(NoteDuration.HALF, seq.notes[1].duration)
        assertEquals(3, seq.beats)
        assertEquals(4, seq.beatType)
    }

    @Test
    fun `newest import comes first`() {
        val store = ImportedExerciseStore(MapSettings())
        store.add("Primo", sequence(), nowMs = 1L)
        store.add("Secondo", sequence(), nowMs = 2L)
        assertEquals(listOf("Secondo", "Primo"), store.getAll().map { it.title })
    }

    @Test
    fun `remove deletes only the requested exercise`() {
        val store = ImportedExerciseStore(MapSettings())
        val a = store.add("A", sequence(), nowMs = 1L)
        store.add("B", sequence(), nowMs = 2L)
        store.remove(a.id)
        assertEquals(listOf("B"), store.getAll().map { it.title })
    }

    @Test
    fun `empty storage yields empty list`() {
        assertTrue(ImportedExerciseStore(MapSettings()).getAll().isEmpty())
    }
}
