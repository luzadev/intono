package com.notemusicali.ui.components

import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteDuration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StaffGeometryTest {
    @Test
    fun `quarter units follow the binary duration ladder`() {
        assertEquals(4.0, StaffLayout.quarterUnits(NoteDuration.WHOLE))
        assertEquals(2.0, StaffLayout.quarterUnits(NoteDuration.HALF))
        assertEquals(1.0, StaffLayout.quarterUnits(NoteDuration.QUARTER))
        assertEquals(0.5, StaffLayout.quarterUnits(NoteDuration.EIGHTH))
        assertEquals(0.25, StaffLayout.quarterUnits(NoteDuration.SIXTEENTH))
        assertEquals(0.125, StaffLayout.quarterUnits(NoteDuration.THIRTY_SECOND))
    }

    private fun n(d: NoteDuration) = MusicalNote.fromNameAndOctave(0, 4, d)

    @Test
    fun `consecutive eighths in the same beat are beamed together`() {
        val notes = listOf(n(NoteDuration.EIGHTH), n(NoteDuration.EIGHTH), n(NoteDuration.QUARTER))
        assertEquals(listOf(0..1), StaffLayout.beamGroups(notes, beats = 4, beatType = 4))
    }

    @Test
    fun `a lone eighth keeps its flag`() {
        val notes = listOf(n(NoteDuration.EIGHTH), n(NoteDuration.QUARTER), n(NoteDuration.EIGHTH))
        assertEquals(emptyList(), StaffLayout.beamGroups(notes, 4, 4))
    }

    @Test
    fun `beat boundary splits a run of eighths in four four`() {
        val notes = List(4) { n(NoteDuration.EIGHTH) }
        assertEquals(listOf(0..1, 2..3), StaffLayout.beamGroups(notes, 4, 4))
    }

    @Test
    fun `six eight groups eighths in threes`() {
        val notes = List(6) { n(NoteDuration.EIGHTH) }
        assertEquals(listOf(0..2, 3..5), StaffLayout.beamGroups(notes, 6, 8))
    }

    @Test
    fun `sixteenths beam with eighths inside the beat`() {
        val notes = listOf(n(NoteDuration.EIGHTH), n(NoteDuration.SIXTEENTH), n(NoteDuration.SIXTEENTH))
        assertEquals(listOf(0..2), StaffLayout.beamGroups(notes, 4, 4))
    }

    @Test
    fun `stem direction for a group follows the majority`() {
        assertTrue(StaffLayout.stemUpForGroup(listOf(0, 2, 6)))
        assertFalse(StaffLayout.stemUpForGroup(listOf(5, 6, 2)))
    }

    @Test
    fun `stem direction tie goes stem up`() {
        assertTrue(StaffLayout.stemUpForGroup(listOf(2, 6)))
    }

    @Test
    fun `degenerate meter yields no beam groups`() {
        assertEquals(emptyList(), StaffLayout.beamGroups(List(4) { n(NoteDuration.EIGHTH) }, beats = 0, beatType = 0))
    }

    @Test
    fun `empty sequence has no beam groups`() {
        assertEquals(emptyList(), StaffLayout.beamGroups(emptyList(), 4, 4))
    }

    @Test
    fun `empty group defaults to stem up`() {
        assertTrue(StaffLayout.stemUpForGroup(emptyList()))
    }

    @Test
    fun `degenerate meter yields no barlines`() {
        assertEquals(emptyList(), StaffLayout.measurePositions(List(4) { n(NoteDuration.QUARTER) }, beats = 0, beatType = 4))
    }

    @Test
    fun `barlines fall every four quarters in four four`() {
        val notes = List(8) { n(NoteDuration.QUARTER) }
        assertEquals(listOf(3, 7), StaffLayout.measurePositions(notes, 4, 4))
    }

    @Test
    fun `barlines fall every three quarters in three four`() {
        val notes = List(6) { n(NoteDuration.QUARTER) }
        assertEquals(listOf(2, 5), StaffLayout.measurePositions(notes, 3, 4))
    }

    @Test
    fun `overshooting note closes the measure after itself`() {
        val notes = listOf(n(NoteDuration.WHOLE), n(NoteDuration.QUARTER), n(NoteDuration.QUARTER))
        assertEquals(listOf(0, 2), StaffLayout.measurePositions(notes, 2, 4))
    }

    @Test
    fun `empty sequence has no barlines`() {
        assertEquals(emptyList(), StaffLayout.measurePositions(emptyList(), 4, 4))
    }

    @Test
    fun `x positions give longer notes more room`() {
        val notes = listOf(n(NoteDuration.HALF), n(NoteDuration.EIGHTH), n(NoteDuration.EIGHTH))
        val xs = StaffLayout.xPositions(notes, areaStart = 0f, areaWidth = 100f)
        assertEquals(3, xs.size)
        val gapLongToShort = xs[1] - xs[0]
        val gapShortToShort = xs[2] - xs[1]
        assertTrue(gapLongToShort > gapShortToShort, "la minima deve occupare più spazio: $xs")
        assertTrue(xs.all { it in 0f..100f })
    }

    @Test
    fun `single note is centered in the area`() {
        val xs = StaffLayout.xPositions(listOf(n(NoteDuration.QUARTER)), 10f, 100f)
        assertEquals(60f, xs.single(), absoluteTolerance = 0.5f)
    }
}
