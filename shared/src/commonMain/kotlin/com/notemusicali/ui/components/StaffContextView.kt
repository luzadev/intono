package com.notemusicali.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.music.MusicalNote

/**
 * Shows a window of notes around the current index, with the current note highlighted.
 * [windowSize] controls how many notes are visible (odd number works best).
 */
@Composable
fun StaffContextView(
    notes: List<MusicalNote>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
    windowSize: Int = 7,
    lightTheme: Boolean = false,
    beats: Int = 4,
    beatType: Int = 4,
) {
    if (notes.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
    ) {
        val lineColor = if (lightTheme) Color.Black else Color.White.copy(alpha = 0.4f)
        val dimNoteColor = if (lightTheme) Color.Black else Color.White.copy(alpha = 0.35f)
        val playedNoteColor = if (lightTheme) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.2f)
        val currentNoteColor = Color(0xFFFFD54F)
        val clefColor = if (lightTheme) Color.Black else Color.White.copy(alpha = 0.8f)

        val lineSpacing = size.height / 10f
        val staffBottom = size.height / 2f + lineSpacing * 2f
        val halfSpacing = lineSpacing / 2f
        val leftMargin = size.width * 0.06f
        val rightMargin = size.width * 0.02f

        val useTreble = StaffLayout.useTrebleClef(notes)

        // Staff lines
        for (i in 0..4) {
            val y = staffBottom - i * lineSpacing
            drawLine(lineColor, Offset(leftMargin, y), Offset(size.width - rightMargin, y), 1.5f)
        }

        // Determine visible window
        val half = windowSize / 2
        val startIdx = (currentIndex - half).coerceAtLeast(0)
        val endIdx = (startIdx + windowSize).coerceAtMost(notes.size)
        val visibleNotes = notes.subList(startIdx, endIdx)

        // Clef (+ time signature when the window includes the start of the piece)
        var cursorX = leftMargin + 2f
        cursorX += drawClef(textMeasurer, useTreble, cursorX, staffBottom, lineSpacing, clefColor)
        if (startIdx == 0) {
            cursorX += 6f
            cursorX += drawTimeSignature(textMeasurer, beats, beatType, cursorX, staffBottom, lineSpacing, clefColor)
        }

        val noteAreaStart = cursorX + 10f
        val noteAreaEnd = size.width - rightMargin - 8f
        val noteAreaWidth = noteAreaEnd - noteAreaStart

        val xs = StaffLayout.xPositions(visibleNotes, noteAreaStart, noteAreaWidth)
        // Beam groups on the full sequence, clipped to the visible window (window-local indices)
        val groups = StaffLayout.beamGroups(notes, beats, beatType)
            .mapNotNull { g ->
                val from = maxOf(g.first, startIdx)
                val to = minOf(g.last, endIdx - 1)
                if (to - from >= 1) (from - startIdx)..(to - startIdx) else null
            }
        val grouped = groups.flatMap { it }.toSet()
        val bars = StaffLayout.measurePositions(notes, beats, beatType).toSet()

        val headHalf = staffHeadHalfWidth(lineSpacing)
        val ledgerHalf = lineSpacing * 0.8f

        visibleNotes.forEachIndexed { vi, note ->
            val actualIdx = startIdx + vi
            val nx = xs[vi]

            val isCurrent = actualIdx == currentIndex
            val isPlayed = actualIdx < currentIndex
            val color = when {
                isCurrent -> currentNoteColor
                isPlayed -> playedNoteColor
                else -> dimNoteColor
            }

            val relPos = StaffLayout.relativePosition(note, useTreble)
            val ny = staffBottom - relPos * halfSpacing

            // Ledger lines
            var lp = -2
            while (lp >= relPos) {
                val ly = staffBottom - lp * halfSpacing
                drawLine(lineColor.copy(alpha = 0.3f), Offset(nx - ledgerHalf, ly), Offset(nx + ledgerHalf, ly), 1f)
                lp -= 2
            }
            lp = 10
            while (lp <= relPos) {
                val ly = staffBottom - lp * halfSpacing
                drawLine(lineColor.copy(alpha = 0.3f), Offset(nx - ledgerHalf, ly), Offset(nx + ledgerHalf, ly), 1f)
                lp += 2
            }

            // Note head
            drawNoteHeadPath(nx, ny, lineSpacing, note.duration, color)

            // Stem + flags (only for notes not part of a beamed group)
            if (vi !in grouped) {
                val stemUp = relPos < 4
                val stemTip = drawStem(nx, ny, lineSpacing, stemUp, note.duration, color)
                stemTip?.let { drawCurvedFlags(it, stemUp, note.duration, lineSpacing, color) }
            }

            // Sharp
            drawAccidentalGlyph(note, nx - headHalf - lineSpacing * 0.5f, ny, lineSpacing * 0.35f, color)

            // Barline between this note and the next
            if (actualIdx in bars && vi < visibleNotes.lastIndex) {
                drawBarline((xs[vi] + xs[vi + 1]) / 2f, staffBottom, lineSpacing, lineColor)
            }

            // Current note highlight circle
            if (isCurrent) {
                drawCircle(currentNoteColor.copy(alpha = 0.15f), radius = lineSpacing * 2f, center = Offset(nx, ny))
            }

            // Note name under current
            if (isCurrent) {
                val labelStyle = TextStyle(color = currentNoteColor, fontSize = 12.sp)
                val labelResult = textMeasurer.measure(note.fullName, labelStyle)
                drawText(labelResult, topLeft = Offset(nx - labelResult.size.width / 2f, staffBottom + lineSpacing * 1.2f))
            }
        }

        // Beamed groups
        val currentVi = currentIndex - startIdx
        groups.forEach { group ->
            val gxs = group.map { xs[it] }
            val relPositions = group.map { StaffLayout.relativePosition(visibleNotes[it], useTreble) }
            val gys = relPositions.map { staffBottom - it * halfSpacing }
            val gdurations = group.map { visibleNotes[it].duration }
            val groupColor = if (currentVi in group) currentNoteColor else dimNoteColor
            drawBeamedGroup(
                xs = gxs,
                ys = gys,
                durations = gdurations,
                stemUp = StaffLayout.stemUpForGroup(relPositions),
                lineSpacing = lineSpacing,
                color = groupColor,
            )
        }
    }
}
