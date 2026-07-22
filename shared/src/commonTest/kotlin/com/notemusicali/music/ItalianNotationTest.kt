package com.notemusicali.music

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ItalianNotationTest {

    @Test
    fun all12NotesAreMapped() {
        val notes = ItalianNotation.allNotes()
        assertEquals(12, notes.size)
    }

    @Test
    fun noteIndex0IsDo() {
        assertEquals("DO", ItalianNotation.fromNoteIndex(0).sharp)
    }

    @Test
    fun noteIndex9IsLa() {
        assertEquals("LA", ItalianNotation.fromNoteIndex(9).sharp)
    }

    @Test
    fun noteIndex11IsSi() {
        assertEquals("SI", ItalianNotation.fromNoteIndex(11).sharp)
    }

    @Test
    fun sharpAndFlatVariantsExistForAccidentals() {
        val cSharp = ItalianNotation.fromNoteIndex(1)
        assertEquals("DO#", cSharp.sharp)
        assertEquals("REb", cSharp.flat)

        val fSharp = ItalianNotation.fromNoteIndex(6)
        assertEquals("FA#", fSharp.sharp)
        assertEquals("SOLb", fSharp.flat)
    }

    @Test
    fun naturalNotesHaveNoFlatVariant() {
        assertNull(ItalianNotation.fromNoteIndex(0).flat)
        assertNull(ItalianNotation.fromNoteIndex(2).flat)
        assertNull(ItalianNotation.fromNoteIndex(4).flat)
        assertNull(ItalianNotation.fromNoteIndex(5).flat)
        assertNull(ItalianNotation.fromNoteIndex(7).flat)
        assertNull(ItalianNotation.fromNoteIndex(9).flat)
        assertNull(ItalianNotation.fromNoteIndex(11).flat)
    }

    @Test
    fun toNoteIndexParsesSharpNames() {
        assertEquals(0, ItalianNotation.toNoteIndex("DO"))
        assertEquals(1, ItalianNotation.toNoteIndex("DO#"))
        assertEquals(7, ItalianNotation.toNoteIndex("SOL"))
        assertEquals(9, ItalianNotation.toNoteIndex("LA"))
    }

    @Test
    fun toNoteIndexParsesFlatNames() {
        assertEquals(1, ItalianNotation.toNoteIndex("REb"))
        assertEquals(3, ItalianNotation.toNoteIndex("MIb"))
        assertEquals(8, ItalianNotation.toNoteIndex("LAb"))
        assertEquals(10, ItalianNotation.toNoteIndex("SIb"))
    }

    @Test
    fun toNoteIndexIsCaseInsensitive() {
        assertEquals(0, ItalianNotation.toNoteIndex("do"))
        assertEquals(7, ItalianNotation.toNoteIndex("sol"))
        assertEquals(1, ItalianNotation.toNoteIndex("do#"))
    }

    @Test
    fun toNoteIndexReturnsNullForInvalidNames() {
        assertNull(ItalianNotation.toNoteIndex("XYZ"))
        assertNull(ItalianNotation.toNoteIndex(""))
    }

    @Test
    fun fromMusicXmlStepMapsCorrectly() {
        assertEquals(0, ItalianNotation.fromMusicXmlStep("C"))
        assertEquals(2, ItalianNotation.fromMusicXmlStep("D"))
        assertEquals(4, ItalianNotation.fromMusicXmlStep("E"))
        assertEquals(5, ItalianNotation.fromMusicXmlStep("F"))
        assertEquals(7, ItalianNotation.fromMusicXmlStep("G"))
        assertEquals(9, ItalianNotation.fromMusicXmlStep("A"))
        assertEquals(11, ItalianNotation.fromMusicXmlStep("B"))
    }

    @Test
    fun fromMusicXmlStepWithAlterHandlesSharpsAndFlats() {
        assertEquals(1, ItalianNotation.fromMusicXmlStep("C", 1))
        assertEquals(11, ItalianNotation.fromMusicXmlStep("C", -1))
        assertEquals(6, ItalianNotation.fromMusicXmlStep("G", -1))
    }

    @Test
    fun fromMusicXmlStepReturnsNullForInvalidStep() {
        assertNull(ItalianNotation.fromMusicXmlStep("X"))
    }

    @Test
    fun indexWrapsCorrectly() {
        assertEquals(
            ItalianNotation.fromNoteIndex(0).sharp,
            ItalianNotation.fromNoteIndex(12).sharp,
        )
    }
}
