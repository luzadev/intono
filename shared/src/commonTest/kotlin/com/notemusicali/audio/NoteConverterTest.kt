package com.notemusicali.audio

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteConverterTest {

    @Test
    fun a4at440Hz() {
        val result = NoteConverter.frequencyToNote(440f)
        assertEquals("LA", result.note.displayName)
        assertEquals(4, result.note.octave)
        assertEquals(69, result.note.midiNumber)
        assertTrue(abs(result.centsDeviation) < 1f, "Cents deviation should be near 0")
    }

    @Test
    fun c4do4at261_63Hz() {
        val result = NoteConverter.frequencyToNote(261.63f)
        assertEquals("DO", result.note.displayName)
        assertEquals(4, result.note.octave)
        assertEquals(60, result.note.midiNumber)
        assertTrue(abs(result.centsDeviation) < 2f, "Cents deviation should be near 0")
    }

    @Test
    fun g3sol3at196HzViolinOpenString() {
        val result = NoteConverter.frequencyToNote(196f)
        assertEquals("SOL", result.note.displayName)
        assertEquals(3, result.note.octave)
    }

    @Test
    fun d4re4at293_66Hz() {
        val result = NoteConverter.frequencyToNote(293.66f)
        assertEquals("RE", result.note.displayName)
        assertEquals(4, result.note.octave)
    }

    @Test
    fun e5mi5at659_26HzViolinEString() {
        val result = NoteConverter.frequencyToNote(659.26f)
        assertEquals("MI", result.note.displayName)
        assertEquals(5, result.note.octave)
    }

    @Test
    fun slightlySharpA4at442HzShowsPositiveCents() {
        val result = NoteConverter.frequencyToNote(442f)
        assertEquals("LA", result.note.displayName)
        assertTrue(result.centsDeviation > 0f, "Should be sharp (positive cents)")
    }

    @Test
    fun slightlyFlatA4at438HzShowsNegativeCents() {
        val result = NoteConverter.frequencyToNote(438f)
        assertEquals("LA", result.note.displayName)
        assertTrue(result.centsDeviation < 0f, "Should be flat (negative cents)")
    }

    @Test
    fun midiToFrequencyAndFrequencyToNoteRoundtrip() {
        for (midi in 48..84) {
            val freq = NoteConverter.midiToFrequency(midi)
            val result = NoteConverter.frequencyToNote(freq)
            assertEquals(midi, result.note.midiNumber, "MIDI $midi should roundtrip")
            assertTrue(
                abs(result.centsDeviation) < 0.1f,
                "Cents should be near 0 for exact MIDI frequency",
            )
        }
    }
}
