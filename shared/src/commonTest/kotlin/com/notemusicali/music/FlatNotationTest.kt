package com.notemusicali.music

import com.notemusicali.scan.NoteParser
import com.notemusicali.ui.components.StaffLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FlatNotationTest {

    @Test
    fun `note built with flat preference displays the flat name`() {
        val note = MusicalNote.fromNameAndOctave(3, 4, preferFlat = true)
        assertEquals("MIb", note.displayName)
        assertEquals("MIb4", note.fullName)
    }

    @Test
    fun `flat preference on a natural note falls back to the natural name`() {
        val note = MusicalNote.fromNameAndOctave(4, 4, preferFlat = true)
        assertEquals("MI", note.displayName)
    }

    @Test
    fun `default preference stays sharp`() {
        assertEquals("RE#", MusicalNote.fromNameAndOctave(3, 4).displayName)
    }

    @Test
    fun `musicxml negative alter produces a flat note`() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <note><pitch><step>B</step><alter>-1</alter><octave>3</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(1, sequence.notes.size)
        assertEquals("SIb", sequence.notes[0].displayName)
        assertEquals(10, sequence.notes[0].noteIndex)
    }

    @Test
    fun `scanned note with b suffix produces a flat note`() {
        val notes = NoteParser.parseNotes("Bb3\nC#4")
        assertEquals(2, notes.size)
        assertEquals("SIb", notes[0].displayName)
        assertEquals("DO#", notes[1].displayName)
    }

    @Test
    fun `flat note sits on the line of the natural above it`() {
        val eFlat = MusicalNote.fromNameAndOctave(3, 4, preferFlat = true)
        val eNatural = MusicalNote.fromNameAndOctave(4, 4)
        assertEquals(StaffLayout.diatonicPosition(eNatural), StaffLayout.diatonicPosition(eFlat))

        val dSharp = MusicalNote.fromNameAndOctave(3, 4)
        val dNatural = MusicalNote.fromNameAndOctave(2, 4)
        assertEquals(StaffLayout.diatonicPosition(dNatural), StaffLayout.diatonicPosition(dSharp))
    }

    @Test
    fun `accidental kind follows the note preference`() {
        assertEquals(StaffLayout.Accidental.SHARP, StaffLayout.accidentalFor(MusicalNote.fromNameAndOctave(3, 4)))
        assertEquals(StaffLayout.Accidental.FLAT, StaffLayout.accidentalFor(MusicalNote.fromNameAndOctave(3, 4, preferFlat = true)))
        assertNull(StaffLayout.accidentalFor(MusicalNote.fromNameAndOctave(0, 4)))
    }
}
