package com.notemusicali.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteDuration

/**
 * Shared staff-layout math for every staff renderer (StaffView, StaffContextView,
 * StaffFullView). Positions are expressed in half-line steps: each unit is half
 * the distance between two staff lines.
 */
object StaffLayout {

    // Chromatic index (0-11) to diatonic step (0-6): sharps sit on their natural's line
    private val DIATONIC_MAP = intArrayOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6)

    // Black-key chromatic indices, rendered with a sharp accidental
    private val ACCIDENTAL_INDICES = setOf(1, 3, 6, 8, 10)

    private const val TREBLE_CLEF = "𝄞" // 𝄞
    private const val BASS_CLEF = "𝄢" // 𝄢

    // Diatonic position of the staff's bottom line: E4 (treble) / G2 (bass)
    private const val TREBLE_BOTTOM_LINE = 30
    private const val BASS_BOTTOM_LINE = 18

    enum class Accidental { SHARP, FLAT }

    fun useTrebleClef(note: MusicalNote): Boolean = note.octave >= 3

    fun useTrebleClef(notes: List<MusicalNote>): Boolean = notes.any { it.octave >= 3 }

    fun diatonicPosition(note: MusicalNote): Int {
        // A flat sits on the line of the natural above it (Eb on E's line),
        // a sharp on the line of the natural below it (D# on D's line).
        // Black keys are never at index 11, so +1 cannot wrap the octave.
        val diatonicIndex = if (note.preferFlat && note.noteIndex in ACCIDENTAL_INDICES) {
            DIATONIC_MAP[note.noteIndex + 1]
        } else {
            DIATONIC_MAP[note.noteIndex]
        }
        return note.octave * 7 + diatonicIndex
    }

    fun accidentalFor(note: MusicalNote): Accidental? = when {
        note.noteIndex !in ACCIDENTAL_INDICES -> null
        note.preferFlat -> Accidental.FLAT
        else -> Accidental.SHARP
    }

    fun bottomLinePosition(useTreble: Boolean): Int =
        if (useTreble) TREBLE_BOTTOM_LINE else BASS_BOTTOM_LINE

    /** Half-line steps above the staff's bottom line (0 = on the bottom line). */
    fun relativePosition(note: MusicalNote, useTreble: Boolean): Int =
        diatonicPosition(note) - bottomLinePosition(useTreble)

    fun needsSharp(note: MusicalNote): Boolean = note.noteIndex in ACCIDENTAL_INDICES

    fun clefSymbol(useTreble: Boolean): String = if (useTreble) TREBLE_CLEF else BASS_CLEF

    /** Durata in quarti (semiminima = 1). */
    fun quarterUnits(duration: NoteDuration): Double = when (duration) {
        NoteDuration.WHOLE -> 4.0
        NoteDuration.HALF -> 2.0
        NoteDuration.QUARTER -> 1.0
        NoteDuration.EIGHTH -> 0.5
        NoteDuration.SIXTEENTH -> 0.25
        NoteDuration.THIRTY_SECOND -> 0.125
    }

    /** Gruppi (indici) di note veloci consecutive che iniziano nello stesso beat. */
    fun beamGroups(notes: List<MusicalNote>, beats: Int, beatType: Int): List<IntRange> {
        if (beats <= 0 || beatType <= 0) return emptyList()
        val beatLen = if (beatType == 8 && beats % 3 == 0) 1.5 else 4.0 / beatType
        val groups = mutableListOf<IntRange>()
        var groupStart = -1
        var groupBeat = -1
        var pos = 0.0
        fun close(endExclusive: Int) {
            if (groupStart >= 0 && endExclusive - groupStart >= 2) groups.add(groupStart until endExclusive)
            groupStart = -1
        }
        notes.forEachIndexed { i, note ->
            val beamable = quarterUnits(note.duration) <= 0.5
            val beat = (pos / beatLen).toInt()
            if (beamable) {
                if (groupStart < 0) { groupStart = i; groupBeat = beat }
                else if (beat != groupBeat) { close(i); groupStart = i; groupBeat = beat }
            } else {
                close(i)
            }
            pos += quarterUnits(note.duration)
        }
        close(notes.size)
        return groups
    }

    /** Gambi in su se almeno metà delle note del gruppo sta sotto la linea mediana. */
    fun stemUpForGroup(relPositions: List<Int>): Boolean =
        relPositions.count { it < 4 } * 2 >= relPositions.size

    /** Indici di nota DOPO i quali cade una stanghetta (inclusa l'eventuale finale). */
    fun measurePositions(notes: List<MusicalNote>, beats: Int, beatType: Int): List<Int> {
        val capacity = beats * (4.0 / beatType)
        if (capacity <= 0.0) return emptyList()
        val positions = mutableListOf<Int>()
        var acc = 0.0
        notes.forEachIndexed { i, note ->
            acc += quarterUnits(note.duration)
            if (acc >= capacity - 1e-9) {
                positions.add(i)
                acc = 0.0
            }
        }
        return positions
    }

    /**
     * Spezza la sequenza in righe con capienza espressa in peso (sqrt della durata),
     * chiudendo preferibilmente all'ultima stanghetta che entra nella riga.
     */
    fun lineBreaks(notes: List<MusicalNote>, beats: Int, beatType: Int, capacityWeight: Float): List<IntRange> {
        if (notes.isEmpty()) return emptyList()
        val weights = notes.map { kotlin.math.sqrt(quarterUnits(it.duration)).toFloat() }
        val bars = measurePositions(notes, beats, beatType).toSet()
        val lines = mutableListOf<IntRange>()
        var start = 0
        while (start < notes.size) {
            var acc = 0f
            var cap = start
            while (cap < notes.size && (cap == start || acc + weights[cap] <= capacityWeight)) {
                acc += weights[cap]
                cap++
            }
            cap--
            val lastBar = ((start + 1)..cap).lastOrNull { it in bars }
            val end = when {
                cap >= notes.size - 1 -> notes.size - 1
                lastBar != null -> lastBar
                else -> cap
            }
            lines.add(start..end)
            start = end + 1
        }
        return lines
    }

    /** Centri orizzontali proporzionali a sqrt(durata): la nota lunga respira. */
    fun xPositions(notes: List<MusicalNote>, areaStart: Float, areaWidth: Float): List<Float> {
        if (notes.isEmpty()) return emptyList()
        val weights = notes.map { kotlin.math.sqrt(quarterUnits(it.duration)).toFloat() }
        val total = weights.sum()
        var cursor = 0f
        return weights.map { w ->
            val center = areaStart + (cursor + w / 2f) / total * areaWidth
            cursor += w
            center
        }
    }
}

/** Draws the accidental glyph appropriate for [note], if any. */
fun DrawScope.drawAccidentalGlyph(note: MusicalNote, cx: Float, cy: Float, s: Float, color: Color) {
    when (StaffLayout.accidentalFor(note)) {
        StaffLayout.Accidental.SHARP -> drawSharpGlyph(cx, cy, s, color)
        StaffLayout.Accidental.FLAT -> drawFlatGlyph(cx, cy, s, color)
        null -> Unit
    }
}

/** Flat glyph (♭) drawn with strokes: vertical stem plus a bowl bulging right. */
fun DrawScope.drawFlatGlyph(cx: Float, cy: Float, s: Float, color: Color) {
    val sw = 1.8f
    val stemX = cx - s * 0.4f
    drawLine(color, Offset(stemX, cy - s * 1.6f), Offset(stemX, cy + s * 0.9f), sw)
    val bowl = Path().apply {
        moveTo(stemX, cy - s * 0.1f)
        cubicTo(
            cx + s * 0.8f, cy - s * 0.5f,
            cx + s * 0.8f, cy + s * 0.5f,
            stemX, cy + s * 0.9f,
        )
    }
    drawPath(bowl, color, style = Stroke(width = sw))
}

/** Sharp glyph drawn with strokes (Unicode ♯ renders unreliably across platforms). */
fun DrawScope.drawSharpGlyph(cx: Float, cy: Float, s: Float, color: Color) {
    val sw = 1.8f
    // Two vertical strokes
    drawLine(color, Offset(cx - s * 0.3f, cy - s * 1.2f), Offset(cx - s * 0.3f, cy + s * 1.2f), sw)
    drawLine(color, Offset(cx + s * 0.3f, cy - s * 1.2f), Offset(cx + s * 0.3f, cy + s * 1.2f), sw)
    // Two horizontal strokes (slightly tilted)
    drawLine(color, Offset(cx - s * 0.7f, cy - s * 0.35f), Offset(cx + s * 0.7f, cy - s * 0.55f), sw + 0.5f)
    drawLine(color, Offset(cx - s * 0.7f, cy + s * 0.55f), Offset(cx + s * 0.7f, cy + s * 0.35f), sw + 0.5f)
}
