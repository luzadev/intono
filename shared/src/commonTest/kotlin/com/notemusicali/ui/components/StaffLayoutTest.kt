package com.notemusicali.ui.components

import com.notemusicali.music.MusicalNote
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StaffLayoutTest {

    private fun note(name: String, octave: Int): MusicalNote {
        val index = when (name) {
            "DO" -> 0; "DO#" -> 1; "RE" -> 2; "MI" -> 4; "FA" -> 5
            "SOL" -> 7; "LA" -> 9; "SI" -> 11
            else -> error("unknown $name")
        }
        return MusicalNote.fromNameAndOctave(index, octave)
    }

    @Test
    fun `diatonic position maps chromatic notes onto seven steps per octave`() {
        assertEquals(28, StaffLayout.diatonicPosition(note("DO", 4)))
        assertEquals(28, StaffLayout.diatonicPosition(note("DO#", 4))) // sharp shares the line of its natural
        assertEquals(34, StaffLayout.diatonicPosition(note("SI", 4)))
        assertEquals(35, StaffLayout.diatonicPosition(note("DO", 5)))
    }

    @Test
    fun `treble clef is chosen from octave 3 up and bass below`() {
        assertTrue(StaffLayout.useTrebleClef(note("SOL", 3)))
        assertFalse(StaffLayout.useTrebleClef(note("DO", 2)))
        assertTrue(StaffLayout.useTrebleClef(listOf(note("DO", 2), note("SOL", 3))))
        assertFalse(StaffLayout.useTrebleClef(listOf(note("DO", 2), note("MI", 2))))
    }

    @Test
    fun `relative position puts E4 on the bottom line of the treble staff`() {
        // E4 (MI4) sits exactly on the first line of the treble staff → relPos 0
        assertEquals(0, StaffLayout.relativePosition(note("MI", 4), useTreble = true))
        // G2 (SOL2) sits on the first line of the bass staff → relPos 0
        assertEquals(0, StaffLayout.relativePosition(note("SOL", 2), useTreble = false))
    }

    @Test
    fun `only black-key notes need an accidental`() {
        assertTrue(StaffLayout.needsSharp(note("DO#", 4)))
        assertFalse(StaffLayout.needsSharp(note("DO", 4)))
        assertFalse(StaffLayout.needsSharp(note("FA", 4)))
    }

    @Test
    fun `clef symbols are the musical unicode glyphs`() {
        assertEquals("𝄞", StaffLayout.clefSymbol(useTreble = true))
        assertEquals("𝄢", StaffLayout.clefSymbol(useTreble = false))
    }
}
