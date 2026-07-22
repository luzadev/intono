package com.notemusicali.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.notemusicali.music.NoteDuration

/** Primitive di disegno del pentagramma condivise da tutte le viste. */

private fun unit(lineSpacing: Float) = lineSpacing * 0.09f

internal fun staffHeadHalfWidth(lineSpacing: Float): Float = 7.5f * unit(lineSpacing)

/** Testa "a pera": piena per <= QUARTER, contorno per WHOLE/HALF. */
internal fun DrawScope.drawNoteHeadPath(
    cx: Float, cy: Float,
    lineSpacing: Float,
    duration: NoteDuration,
    color: Color,
) {
    val u = unit(lineSpacing)
    val path = Path().apply {
        moveTo(cx - 7.4f * u, cy + 2.6f * u)
        quadraticTo(cx - 5.4f * u, cy - 4.8f * u, cx + 2.4f * u, cy - 4.9f * u)
        quadraticTo(cx + 7.6f * u, cy - 4.9f * u, cx + 7.4f * u, cy - 0.9f * u)
        quadraticTo(cx + 6.8f * u, cy + 4.9f * u, cx - 1.6f * u, cy + 5.0f * u)
        quadraticTo(cx - 7.4f * u, cy + 5.0f * u, cx - 7.4f * u, cy + 2.6f * u)
        close()
    }
    val hollow = duration == NoteDuration.WHOLE || duration == NoteDuration.HALF
    if (hollow) drawPath(path, color, style = Stroke(width = lineSpacing * 0.16f))
    else drawPath(path, color)
}

/** Gambo attaccato al bordo della testa; ritorna l'estremità (null per la semibreve). */
internal fun DrawScope.drawStem(
    cx: Float, cy: Float,
    lineSpacing: Float,
    stemUp: Boolean,
    duration: NoteDuration,
    color: Color,
    tipY: Float? = null,
): Offset? {
    if (duration == NoteDuration.WHOLE) return null
    val u = unit(lineSpacing)
    val stemH = lineSpacing * 3.2f
    val x = if (stemUp) cx + 7.0f * u else cx - 7.0f * u
    val endY = tipY ?: if (stemUp) cy - stemH else cy + stemH
    drawLine(color, Offset(x, cy), Offset(x, endY), lineSpacing * 0.14f)
    return Offset(x, endY)
}

/** Code a goccia riempite (1-3), specchiate per gambo in giù. */
internal fun DrawScope.drawCurvedFlags(
    stemTip: Offset,
    stemUp: Boolean,
    duration: NoteDuration,
    lineSpacing: Float,
    color: Color,
) {
    val count = when (duration) {
        NoteDuration.EIGHTH -> 1
        NoteDuration.SIXTEENTH -> 2
        NoteDuration.THIRTY_SECOND -> 3
        else -> return
    }
    val u = unit(lineSpacing)
    val dir = if (stemUp) 1f else -1f
    for (i in 0 until count) {
        val oy = stemTip.y + dir * i * 5.5f * u
        val path = Path().apply {
            moveTo(stemTip.x, oy)
            cubicTo(
                stemTip.x + 7f * u, oy + dir * 4f * u,
                stemTip.x + 10f * u, oy + dir * 11f * u,
                stemTip.x + 6.5f * u, oy + dir * 21f * u,
            )
            cubicTo(
                stemTip.x + 10.5f * u, oy + dir * 12f * u,
                stemTip.x + 8.5f * u, oy + dir * 6f * u,
                stemTip.x, oy + dir * 6f * u,
            )
            close()
        }
        drawPath(path, color)
    }
}

/**
 * Gruppo travato: gambi allineati a una travatura inclinata (pendenza clampata
 * a una lineSpacing) più la seconda barra tra coppie di semicrome.
 */
internal fun DrawScope.drawBeamedGroup(
    xs: List<Float>, ys: List<Float>,
    durations: List<NoteDuration>,
    stemUp: Boolean,
    lineSpacing: Float,
    color: Color,
) {
    if (xs.size < 2) return
    val u = unit(lineSpacing)
    val stemH = lineSpacing * 3.2f
    val dir = if (stemUp) -1f else 1f
    val attachX = xs.map { if (stemUp) it + 7.0f * u else it - 7.0f * u }

    // Estremità ideali, poi pendenza clampata; la travatura deve superare tutte le teste
    var y0 = ys.first() + dir * stemH
    var y1 = ys.last() + dir * stemH
    if (y1 - y0 > lineSpacing) y1 = y0 + lineSpacing
    if (y0 - y1 > lineSpacing) y0 = y1 + lineSpacing
    val minClearance = lineSpacing * 2.2f
    ys.forEachIndexed { i, noteY ->
        val t = (attachX[i] - attachX.first()) / (attachX.last() - attachX.first())
        val beamY = y0 + (y1 - y0) * t
        val clearance = (noteY - beamY) * -dir
        if (clearance < minClearance) {
            val shift = (minClearance - clearance) * dir
            y0 += shift; y1 += shift
        }
    }

    fun beamYAt(x: Float) = y0 + (y1 - y0) * (x - attachX.first()) / (attachX.last() - attachX.first())

    // Gambi fino alla travatura
    xs.indices.forEach { i ->
        drawLine(color, Offset(attachX[i], ys[i]), Offset(attachX[i], beamYAt(attachX[i])), lineSpacing * 0.14f)
    }

    // La travatura si ispessisce VERSO le teste: in giù se i gambi sono in su
    val thicken = lineSpacing * 0.45f * (if (stemUp) 1f else -1f)
    fun beamSegment(fromX: Float, toX: Float, offset: Float) {
        val path = Path().apply {
            moveTo(fromX, beamYAt(fromX) + offset)
            lineTo(toX, beamYAt(toX) + offset)
            lineTo(toX, beamYAt(toX) + offset + thicken)
            lineTo(fromX, beamYAt(fromX) + offset + thicken)
            close()
        }
        drawPath(path, color)
    }
    // Barra primaria su tutto il gruppo
    beamSegment(attachX.first(), attachX.last(), 0f)
    // Barra secondaria tra coppie adiacenti di semicrome (o più corte)
    val secondOffset = lineSpacing * 0.75f * (if (stemUp) 1f else -1f)
    for (i in 0 until xs.size - 1) {
        val bothShort = StaffLayout.quarterUnits(durations[i]) <= 0.25 && StaffLayout.quarterUnits(durations[i + 1]) <= 0.25
        if (bothShort) beamSegment(attachX[i], attachX[i + 1], secondOffset)
    }
}

/** Stanghetta di battuta. */
internal fun DrawScope.drawBarline(x: Float, staffBottom: Float, lineSpacing: Float, color: Color) {
    drawLine(color, Offset(x, staffBottom - 4 * lineSpacing), Offset(x, staffBottom), lineSpacing * 0.1f)
}

/** Cifre del tempo (es. 4/4) dopo la chiave. Ritorna la larghezza occupata. */
internal fun DrawScope.drawTimeSignature(
    textMeasurer: TextMeasurer,
    beats: Int, beatType: Int,
    x: Float, staffBottom: Float, lineSpacing: Float,
    color: Color,
): Float {
    val style = TextStyle(
        color = color,
        fontSize = (lineSpacing * 1.9f).toSp(),
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
    )
    val top = textMeasurer.measure(beats.toString(), style)
    val bottom = textMeasurer.measure(beatType.toString(), style)
    drawText(top, topLeft = Offset(x, staffBottom - 4f * lineSpacing + (2f * lineSpacing - top.size.height) / 2f))
    drawText(bottom, topLeft = Offset(x, staffBottom - 2f * lineSpacing + (2f * lineSpacing - bottom.size.height) / 2f))
    return maxOf(top.size.width, bottom.size.width).toFloat()
}

/** Chiave a piena opacità. Ritorna la larghezza occupata. */
internal fun DrawScope.drawClef(
    textMeasurer: TextMeasurer,
    useTreble: Boolean,
    x: Float, staffBottom: Float, lineSpacing: Float,
    color: Color,
): Float {
    val style = TextStyle(color = color, fontSize = (lineSpacing * 3.4f).toSp(), fontFamily = FontFamily.Serif)
    val result = textMeasurer.measure(StaffLayout.clefSymbol(useTreble), style)
    val y = if (useTreble) {
        (staffBottom - lineSpacing) - result.size.height * 0.42f
    } else {
        (staffBottom - 3 * lineSpacing) - result.size.height * 0.35f
    }
    drawText(result, topLeft = Offset(x, y))
    return result.size.width.toFloat()
}
