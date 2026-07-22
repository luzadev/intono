package com.notemusicali.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.notemusicali.music.MusicalNote

/**
 * Multi-line staff showing all notes with vertical scroll and auto-scroll to
 * the current line. Lines wrap at measure boundaries when possible, notes are
 * spaced proportionally to their duration, and fast notes are beamed.
 */
@Composable
fun StaffFullView(
    notes: List<MusicalNote>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
    lightTheme: Boolean = false,
    beats: Int = 4,
    beatType: Int = 4,
    // Altezza massima visibile: oltre, lo spartito scorre da solo seguendo la nota corrente
    maxVisibleHeight: Dp = 340.dp,
) {
    if (notes.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    val systemHeightDp = 100.dp
    val systemGapDp = 16.dp
    val clefWidthDp = 36.dp
    val noteSpacingDp = 36.dp

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val availableWidthDp = maxWidth
        val noteAreaWidthDp = availableWidthDp - clefWidthDp - 8.dp
        // Capienza in peso: una unità di peso (semiminima) occupa ~noteSpacingDp
        val capacityWeight = (noteAreaWidthDp / noteSpacingDp).coerceAtLeast(4f)

        val bars = StaffLayout.measurePositions(notes, beats, beatType)
        val allGroups = StaffLayout.beamGroups(notes, beats, beatType)
        val lines = StaffLayout.lineBreaks(notes, beats, beatType, capacityWeight)
        val lineCount = lines.size
        val totalHeightDp = systemHeightDp * lineCount + systemGapDp * (lineCount - 1).coerceAtLeast(0)

        // Auto-scroll to current line
        val lineOfNote = IntArray(notes.size)
        lines.forEachIndexed { li, r -> r.forEach { lineOfNote[it] = li } }
        val currentLine = lineOfNote.getOrElse(currentIndex) { 0 }
        LaunchedEffect(currentLine) {
            val systemHeightPx = with(density) { (systemHeightDp + systemGapDp).toPx() }
            val viewportHeight = scrollState.viewportSize
            val targetY = (systemHeightPx * currentLine - viewportHeight / 3f)
                .toInt().coerceAtLeast(0)
            scrollState.animateScrollTo(targetY)
        }

        // Con un vincolo di altezza dal contenitore (es. weight in Pratica) il
        // viewport si adatta alla finestra; altrimenti vale il tetto di default
        val heightBound = if (this.maxHeight != Dp.Infinity) this.maxHeight else maxVisibleHeight
        val viewportHeightDp = if (totalHeightDp < heightBound) totalHeightDp else heightBound
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(viewportHeightDp)
                .verticalScroll(scrollState),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeightDp),
            ) {
            val lineColor = if (lightTheme) Color.Black else Color.White.copy(alpha = 0.4f)
            val dimColor = if (lightTheme) Color.Black else Color.White.copy(alpha = 0.35f)
            val playedColor = if (lightTheme) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.2f)
            val currentColor = Color(0xFFFFD54F)
            val clefColor = if (lightTheme) Color.Black else Color.White.copy(alpha = 0.8f)

            val systemHeightPx = systemHeightDp.toPx()
            val systemGapPx = systemGapDp.toPx()

            val lineSpacing = systemHeightPx / 8f
            val halfSpacing = lineSpacing / 2f
            val staffRelBottom = systemHeightPx / 2f + lineSpacing * 2f

            val useTreble = StaffLayout.useTrebleClef(notes)

            val headHalf = staffHeadHalfWidth(lineSpacing)
            val ledgerHalf = lineSpacing * 0.75f

            lines.forEachIndexed { li, r ->
                val systemTop = li * (systemHeightPx + systemGapPx)
                val staffBottom = systemTop + staffRelBottom

                // Staff lines
                for (i in 0..4) {
                    val y = staffBottom - i * lineSpacing
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1.2f)
                }

                // Clef (+ time signature on the first line only)
                var cursor = 4f
                cursor += drawClef(textMeasurer, useTreble, cursor, staffBottom, lineSpacing, clefColor)
                if (li == 0) {
                    cursor += 6f
                    cursor += drawTimeSignature(textMeasurer, beats, beatType, cursor, staffBottom, lineSpacing, clefColor)
                }

                val lineNotes = notes.subList(r.first, r.last + 1)
                val xs = StaffLayout.xPositions(lineNotes, cursor + 10f, size.width - cursor - 18f)
                // Travature globali (origine di beat corretta) clippate alla riga
                val groups = allGroups.mapNotNull { g ->
                    val from = maxOf(g.first, r.first)
                    val to = minOf(g.last, r.last)
                    if (to - from >= 1) (from - r.first)..(to - r.first) else null
                }
                val grouped = groups.flatMap { it }.toSet()
                val currentInLine = currentIndex - r.first

                lineNotes.forEachIndexed { vi, note ->
                    val actualIdx = r.first + vi
                    val nx = xs[vi]

                    val isCurrent = actualIdx == currentIndex
                    val isPlayed = actualIdx < currentIndex
                    val color = when {
                        isCurrent -> currentColor
                        isPlayed -> playedColor
                        else -> dimColor
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

                    // Accidental
                    drawAccidentalGlyph(note, nx - headHalf - lineSpacing * 0.5f, ny, lineSpacing * 0.35f, color)

                    // Current note highlight circle
                    if (isCurrent) {
                        drawCircle(currentColor.copy(alpha = 0.15f), radius = lineSpacing * 1.8f, center = Offset(nx, ny))
                    }
                }

                // Beamed groups
                groups.forEach { group ->
                    val gxs = group.map { xs[it] }
                    val relPositions = group.map { StaffLayout.relativePosition(lineNotes[it], useTreble) }
                    val gys = relPositions.map { staffBottom - it * halfSpacing }
                    val gdurations = group.map { lineNotes[it].duration }
                    val groupColor = if (currentInLine in group) currentColor else dimColor
                    drawBeamedGroup(
                        xs = gxs,
                        ys = gys,
                        durations = gdurations,
                        stemUp = StaffLayout.stemUpForGroup(relPositions),
                        lineSpacing = lineSpacing,
                        color = groupColor,
                    )
                }

                // Barlines
                bars.forEach { b ->
                    if (b in r && b < r.last) {
                        drawBarline((xs[b - r.first] + xs[b - r.first + 1]) / 2f, staffBottom, lineSpacing, lineColor)
                    }
                }
                if (r.last in bars) {
                    drawBarline(size.width - 8f, staffBottom, lineSpacing, lineColor)
                }
            }
        }
        }
    }
}
