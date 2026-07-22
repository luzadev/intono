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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.music.MusicalNote

@Composable
fun StaffView(
    note: MusicalNote,
    modifier: Modifier = Modifier,
    lightTheme: Boolean = false,
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        val lineColor = if (lightTheme) Color.Black else Color.White.copy(alpha = 0.5f)
        val noteColor = if (lightTheme) Color.Black else Color.White

        val lineSpacing = size.height / 12f
        val staffBottom = size.height / 2f + lineSpacing * 2f
        val halfSpacing = lineSpacing / 2f
        val leftMargin = size.width * 0.06f
        val rightMargin = size.width * 0.02f
        val noteX = size.width * 0.5f

        val useTreble = StaffLayout.useTrebleClef(note)
        val relPos = StaffLayout.relativePosition(note, useTreble)

        val noteY = staffBottom - relPos * halfSpacing

        // --- Staff lines ---
        for (i in 0..4) {
            val y = staffBottom - i * lineSpacing
            drawLine(
                color = lineColor,
                start = Offset(leftMargin, y),
                end = Offset(size.width - rightMargin, y),
                strokeWidth = 1.5f,
            )
        }

        // --- Clef symbol ---
        drawClef(textMeasurer, useTreble, leftMargin + 2f, staffBottom, lineSpacing, noteColor)

        // --- Ledger lines ---
        val ledgerHalf = lineSpacing * 0.9f
        // Below staff
        var lp = -2
        while (lp >= relPos) {
            val ly = staffBottom - lp * halfSpacing
            drawLine(
                color = lineColor,
                start = Offset(noteX - ledgerHalf, ly),
                end = Offset(noteX + ledgerHalf, ly),
                strokeWidth = 1.5f,
            )
            lp -= 2
        }
        // Above staff
        lp = 10
        while (lp <= relPos) {
            val ly = staffBottom - lp * halfSpacing
            drawLine(
                color = lineColor,
                start = Offset(noteX - ledgerHalf, ly),
                end = Offset(noteX + ledgerHalf, ly),
                strokeWidth = 1.5f,
            )
            lp += 2
        }

        // --- Note head + stem + flags ---
        drawNoteHeadPath(noteX, noteY, lineSpacing, note.duration, noteColor)
        val stemUp = relPos < 4
        val tip = drawStem(noteX, noteY, lineSpacing, stemUp, note.duration, noteColor)
        tip?.let { drawCurvedFlags(it, stemUp, note.duration, lineSpacing, noteColor) }

        // --- Accidental (sharp) ---
        drawAccidentalGlyph(note, noteX - staffHeadHalfWidth(lineSpacing) - lineSpacing * 0.7f, noteY, lineSpacing * 0.45f, noteColor)

        // --- Note name label under staff ---
        val labelStyle = TextStyle(
            color = if (lightTheme) Color.Black else Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        val labelResult = textMeasurer.measure(note.fullName, labelStyle)
        val labelY = staffBottom + lineSpacing * 1.5f
        drawText(
            textLayoutResult = labelResult,
            topLeft = Offset(
                noteX - labelResult.size.width / 2f,
                labelY,
            ),
        )
    }
}

/**
 * Compact staff preview showing all notes of a sequence.
 */
@Composable
fun StaffPreview(
    notes: List<MusicalNote>,
    modifier: Modifier = Modifier,
    beats: Int = 4,
    beatType: Int = 4,
) {
    if (notes.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
    ) {
        val lineColor = Color.White.copy(alpha = 0.3f)
        val noteColor = Color.White.copy(alpha = 0.85f)

        val lineSpacing = size.height / 8f
        val staffBottom = size.height / 2f + lineSpacing * 2f
        val halfSpacing = lineSpacing / 2f
        val leftMargin = size.width * 0.06f
        val rightMargin = size.width * 0.02f

        val useTreble = StaffLayout.useTrebleClef(notes)

        // --- Staff lines ---
        for (i in 0..4) {
            val y = staffBottom - i * lineSpacing
            drawLine(
                color = lineColor,
                start = Offset(leftMargin, y),
                end = Offset(size.width - rightMargin, y),
                strokeWidth = 1f,
            )
        }

        // --- Clef symbol ---
        drawClef(textMeasurer, useTreble, 4f, staffBottom, lineSpacing, noteColor)

        // --- Notes ---
        val noteAreaStart = leftMargin + 8f
        val noteAreaEnd = size.width - rightMargin - 8f
        val noteAreaWidth = noteAreaEnd - noteAreaStart

        val xs = StaffLayout.xPositions(notes, noteAreaStart, noteAreaWidth)
        val relPositions = notes.map { StaffLayout.relativePosition(it, useTreble) }
        val ys = relPositions.map { staffBottom - it * halfSpacing }

        val groups = StaffLayout.beamGroups(notes, beats, beatType)
        val grouped = groups.flatMap { it }.toSet()

        val ledgerHalf = lineSpacing * 0.7f

        notes.forEachIndexed { index, note ->
            val nx = xs[index]
            val relPos = relPositions[index]
            val ny = ys[index]

            // Ledger lines for this note
            var lp = -2
            while (lp >= relPos) {
                val ly = staffBottom - lp * halfSpacing
                drawLine(lineColor, Offset(nx - ledgerHalf, ly), Offset(nx + ledgerHalf, ly), 1f)
                lp -= 2
            }
            lp = 10
            while (lp <= relPos) {
                val ly = staffBottom - lp * halfSpacing
                drawLine(lineColor, Offset(nx - ledgerHalf, ly), Offset(nx + ledgerHalf, ly), 1f)
                lp += 2
            }

            // Note head
            drawNoteHeadPath(nx, ny, lineSpacing, note.duration, noteColor)

            // Stem + flags (beamed notes get their stems from the beam group)
            if (index !in grouped) {
                val stemUp = relPos < 4
                val tip = drawStem(nx, ny, lineSpacing, stemUp, note.duration, noteColor)
                tip?.let { drawCurvedFlags(it, stemUp, note.duration, lineSpacing, noteColor) }
            }

            // Sharp
            drawAccidentalGlyph(note, nx - staffHeadHalfWidth(lineSpacing) - lineSpacing * 0.5f, ny, lineSpacing * 0.3f, noteColor)
        }

        // --- Beamed groups ---
        groups.forEach { group ->
            val gxs = group.map { xs[it] }
            val gys = group.map { ys[it] }
            val gdur = group.map { notes[it].duration }
            val stemUp = StaffLayout.stemUpForGroup(group.map { relPositions[it] })
            drawBeamedGroup(gxs, gys, gdur, stemUp, lineSpacing, noteColor)
        }
    }
}
