# Staff Rendering Upgrade — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rendere il pentagramma con glifi raffinati, travature, stanghette di battuta con tempo, e spaziatura proporzionale alla durata, in tutte le viste Compose condivise.

**Architecture:** La geometria (unità di durata, gruppi di travatura, posizioni battute, pesi/posizioni orizzontali, direzione gambi) vive in `StaffLayout` (pura, TDD). Le primitive di disegno (`Path` per teste/code, parallelogrammi per travature, stanghette, cifre tempo, chiave) vivono nel nuovo `StaffRenderer.kt` e sostituiscono `NoteRenderer.kt`. Le tre viste diventano orchestratori sottili.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform Canvas/DrawScope, kotlin-test. Spec: `docs/superpowers/specs/2026-07-22-staff-rendering-design.md`.

**Nota:** il progetto NON è un repository git — al posto dei commit ogni task termina eseguendo la suite (`export JAVA_HOME=$(/usr/libexec/java_home) && ./gradlew :shared:desktopTest`).

---

### Task 1: StaffLayout.quarterUnits

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/notemusicali/ui/components/StaffLayout.kt`
- Test: `shared/src/commonTest/kotlin/com/notemusicali/ui/components/StaffGeometryTest.kt` (create)

- [ ] **Step 1: test fallente**

```kotlin
package com.notemusicali.ui.components

import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteDuration
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
```

- [ ] **Step 2: verifica RED** — `./gradlew :shared:compileTestKotlinDesktop` → errore `Unresolved reference 'quarterUnits'`
- [ ] **Step 3: implementazione minima** in `StaffLayout`:

```kotlin
    fun quarterUnits(duration: NoteDuration): Double = when (duration) {
        NoteDuration.WHOLE -> 4.0
        NoteDuration.HALF -> 2.0
        NoteDuration.QUARTER -> 1.0
        NoteDuration.EIGHTH -> 0.5
        NoteDuration.SIXTEENTH -> 0.25
        NoteDuration.THIRTY_SECOND -> 0.125
    }
```

(aggiungere `import com.notemusicali.music.NoteDuration`)

- [ ] **Step 4: verifica GREEN** — `./gradlew :shared:desktopTest --tests "com.notemusicali.ui.components.StaffGeometryTest"` → PASS

### Task 2: StaffLayout.beamGroups + stemUpForGroup

**Files:** come Task 1.

- [ ] **Step 1: test fallenti** (aggiungere a `StaffGeometryTest`; helper `n(dur)` costruisce una nota DO4 con quella durata)

```kotlin
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
        // 4 crome: beat 1 = note 0-1, beat 2 = note 2-3
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
        assertTrue(StaffLayout.stemUpForGroup(listOf(0, 2, 6)))   // 2 su 3 sotto la linea mediana
        assertTrue(!StaffLayout.stemUpForGroup(listOf(5, 6, 2)))  // 2 su 3 sopra
    }
```

- [ ] **Step 2: verifica RED** — compile error su `beamGroups`
- [ ] **Step 3: implementazione** in `StaffLayout`:

```kotlin
    /** Gruppi (indici) di note veloci consecutive che iniziano nello stesso beat. */
    fun beamGroups(notes: List<MusicalNote>, beats: Int, beatType: Int): List<IntRange> {
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

    /** Gambi in su se la maggioranza delle note del gruppo sta sotto la linea mediana. */
    fun stemUpForGroup(relPositions: List<Int>): Boolean =
        relPositions.count { it < 4 } * 2 >= relPositions.size
```

- [ ] **Step 4: verifica GREEN** — suite del file → PASS

### Task 3: StaffLayout.measurePositions

**Files:** come Task 1.

- [ ] **Step 1: test fallenti**

```kotlin
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
        // 2/4: la semibreve (4 quarti) eccede la battuta da 2 → stanghetta dopo di lei
        val notes = listOf(n(NoteDuration.WHOLE), n(NoteDuration.QUARTER), n(NoteDuration.QUARTER))
        assertEquals(listOf(0, 2), StaffLayout.measurePositions(notes, 2, 4))
    }

    @Test
    fun `empty sequence has no barlines`() {
        assertEquals(emptyList(), StaffLayout.measurePositions(emptyList(), 4, 4))
    }
```

- [ ] **Step 2: verifica RED**
- [ ] **Step 3: implementazione**

```kotlin
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
```

- [ ] **Step 4: verifica GREEN**

### Task 4: StaffLayout.xPositions (spaziatura proporzionale)

**Files:** come Task 1.

- [ ] **Step 1: test fallenti**

```kotlin
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
```

- [ ] **Step 2: verifica RED**
- [ ] **Step 3: implementazione** (peso = √durata; centro dello slot)

```kotlin
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
```

- [ ] **Step 4: verifica GREEN**

### Task 5: NoteSequence.beats/beatType + parser `<time>`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/notemusicali/music/NoteSequence.kt`
- Modify: `shared/src/commonMain/kotlin/com/notemusicali/music/MusicXmlParser.kt`
- Test: `shared/src/commonTest/kotlin/com/notemusicali/music/MusicXmlParserTest.kt`

- [ ] **Step 1: test fallenti** (in `MusicXmlParserTest`)

```kotlin
    @Test
    fun parseExtractsTimeSignature() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <attributes><time><beats>3</beats><beat-type>4</beat-type></time></attributes>
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()
        val sequence = MusicXmlParser.parse(xml)
        assertEquals(3, sequence.beats)
        assertEquals(4, sequence.beatType)
    }

    @Test
    fun parseDefaultsToFourFourWithoutTimeSignature() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()
        val sequence = MusicXmlParser.parse(xml)
        assertEquals(4, sequence.beats)
        assertEquals(4, sequence.beatType)
    }
```

- [ ] **Step 2: verifica RED** — `Unresolved reference 'beats'`
- [ ] **Step 3: implementazione**

`NoteSequence.kt`: aggiungere i campi con default

```kotlin
data class NoteSequence(
    val name: String,
    val notes: List<MusicalNote>,
    val beats: Int = 4,
    val beatType: Int = 4,
) { /* corpo esistente invariato */ }
```

`MusicXmlParser.kt`: nuova regex + estrazione nel `parse`

```kotlin
    private val TIME_REGEX = Regex(
        """<time\b[^>]*>.*?<beats>\s*(\d+)\s*</beats>.*?<beat-type>\s*(\d+)\s*</beat-type>.*?</time>""",
        RegexOption.DOT_MATCHES_ALL,
    )
```

e nel `parse`, prima del `return`:

```kotlin
        val time = TIME_REGEX.find(partContent)
        val beats = time?.groupValues?.get(1)?.toIntOrNull() ?: 4
        val beatType = time?.groupValues?.get(2)?.toIntOrNull() ?: 4
        return NoteSequence(name = name, notes = notes, beats = beats, beatType = beatType)
```

- [ ] **Step 4: verifica GREEN** — suite completa desktop → PASS

### Task 6: StaffRenderer.kt — primitive di disegno

**Files:**
- Create: `shared/src/commonMain/kotlin/com/notemusicali/ui/components/StaffRenderer.kt`

Non testabile a unità (DrawScope): la verifica è la compilazione + l'ispezione visiva del Task 10. Coordinate dei `Path` prese dai mockup approvati (unità `u = lineSpacing * 0.09f`, testa larga ~1.35·lineSpacing come oggi).

- [ ] **Step 1: creare il file** con questo contenuto:

```kotlin
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
import androidx.compose.ui.unit.sp
import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteDuration

/** Primitive di disegno del pentagramma condivise da tutte le viste. */

private fun unit(lineSpacing: Float) = lineSpacing * 0.09f

internal fun staffHeadHalfWidth(lineSpacing: Float): Float = 7.5f * unit(lineSpacing)

/** Testa "a pera" (dal mockup approvato): piena per <= QUARTER, contorno per WHOLE/HALF. */
internal fun DrawScope.drawNoteHeadPath(
    cx: Float, cy: Float,
    lineSpacing: Float,
    duration: NoteDuration,
    color: Color,
) {
    val u = unit(lineSpacing)
    val path = Path().apply {
        moveTo(cx - 7.4f * u, cy + 2.6f * u)
        quadraticBezierTo(cx - 5.4f * u, cy - 4.8f * u, cx + 2.4f * u, cy - 4.9f * u)
        quadraticBezierTo(cx + 7.6f * u, cy - 4.9f * u, cx + 7.4f * u, cy - 0.9f * u)
        quadraticBezierTo(cx + 6.8f * u, cy + 4.9f * u, cx - 1.6f * u, cy + 5.0f * u)
        quadraticBezierTo(cx - 7.4f * u, cy + 5.0f * u, cx - 7.4f * u, cy + 2.6f * u)
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
 * Gruppo travato: disegna gambi allineati a una travatura inclinata (pendenza
 * clampata a una lineSpacing) più la seconda barra tra coppie di semicrome.
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
        val bothShort = quartersOf(durations[i]) <= 0.25 && quartersOf(durations[i + 1]) <= 0.25
        if (bothShort) beamSegment(attachX[i], attachX[i + 1], secondOffset)
    }
}

private fun quartersOf(d: NoteDuration): Double = StaffLayout.quarterUnits(d)

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
```

- [ ] **Step 2: compilare** — `./gradlew :shared:compileKotlinDesktop` → BUILD SUCCESSFUL

### Task 7: riscrittura StaffView.kt (StaffView + StaffPreview)

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/notemusicali/ui/components/StaffView.kt`

- [ ] **Step 1: StaffView (nota singola)** — sostituire il blocco head/stem/flags/accidental con:

```kotlin
        drawNoteHeadPath(noteX, noteY, lineSpacing, note.duration, noteColor)
        val stemUp = relPos < 4
        val tip = drawStem(noteX, noteY, lineSpacing, stemUp, note.duration, noteColor)
        tip?.let { drawCurvedFlags(it, stemUp, note.duration, lineSpacing, noteColor) }
        drawAccidentalGlyph(note, noteX - staffHeadHalfWidth(lineSpacing) - lineSpacing * 0.7f, noteY, lineSpacing * 0.45f, noteColor)
```

e il blocco chiave con `drawClef(textMeasurer, useTreble, leftMargin + 2f, staffBottom, lineSpacing, clefColor)` (dove `clefColor` = colore note a piena opacità).

- [ ] **Step 2: StaffPreview** — sostituire il calcolo `noteStep` uniforme con `StaffLayout.xPositions(notes, noteAreaStart, noteAreaWidth)`; calcolare `val groups = StaffLayout.beamGroups(notes, 4, 4)` e disegnare le note nei gruppi con `drawBeamedGroup` (x/y/durate del gruppo, `stemUp = StaffLayout.stemUpForGroup(relPos del gruppo)`), le altre con `drawStem`+`drawCurvedFlags`. Le teste si disegnano per tutte con `drawNoteHeadPath`.
- [ ] **Step 3: compilare + suite** — BUILD SUCCESSFUL, test verdi.

### Task 8: riscrittura StaffContextView.kt

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/notemusicali/ui/components/StaffContextView.kt`

- [ ] **Step 1:** firma: aggiungere parametri `beats: Int = 4, beatType: Int = 4` a `StaffContextView` (i chiamanti in PracticeScreen li passano dalla sequenza, con default invariati altrove).
- [ ] **Step 2:** dentro il Canvas: `xs = StaffLayout.xPositions(visibleNotes, noteAreaStart, noteAreaWidth)`; `groups = StaffLayout.beamGroups(visibleNotes, beats, beatType)`; `bars = StaffLayout.measurePositions(notes, beats, beatType)` (calcolate sull'intera sequenza, disegnate se l'indice assoluto cade nella finestra: `x` a metà tra la nota e la successiva). Disegno note: teste `drawNoteHeadPath` sempre; per gli indici nei gruppi raccogliere x/y/durate e chiamare `drawBeamedGroup` una volta per gruppo con il colore della nota corrente se il gruppo la contiene, altrimenti dim; per gli altri `drawStem`+`drawCurvedFlags`. Chiave con `drawClef`. Evidenziazioni (cerchio oro, etichetta) invariate.
- [ ] **Step 3:** aggiornare il call-site in `PracticeScreen` (passare `beats`/`beatType` della sequenza corrente).
- [ ] **Step 4:** compilare + suite.

### Task 9: riscrittura StaffFullView.kt

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/notemusicali/ui/components/StaffFullView.kt`

- [ ] **Step 1:** firma: aggiungere `beats: Int = 4, beatType: Int = 4`; call-site in PracticeScreen aggiornato.
- [ ] **Step 2:** wrapping per battute: pre-calcolare `bars = StaffLayout.measurePositions(notes, beats, beatType)` e spezzare le righe sul confine di battuta più vicino alla capienza (`notesPerLine` diventa il tetto: la riga termina all'ultima stanghetta che ci sta; se una battuta eccede da sola la riga si spezza comunque a `notesPerLine`). Ogni riga usa `StaffLayout.xPositions` sul proprio segmento, `beamGroups` calcolati per segmento di riga, tempo (4/4) disegnato con `drawTimeSignature` solo sulla prima riga dopo la chiave, `drawBarline` alle stanghette interne e a fine riga se coincide con una battuta.
- [ ] **Step 3:** compilare + suite; auto-scroll esistente invariato (usa `currentLine` = riga contenente `currentIndex`, ora derivata dalla mappa nota→riga costruita nello step 2).

### Task 10: pulizia + verifica completa

- [ ] **Step 1:** eliminare `NoteRenderer.kt` (drawNoteHead/drawNoteStem/drawNoteFlags non più referenziati — verificare con grep prima di cancellare).
- [ ] **Step 2:** suite completa: `./gradlew :shared:desktopTest :shared:iosSimulatorArm64Test :app:assembleDebug` → tutto verde.
- [ ] **Step 3:** lancio app desktop (`./gradlew :desktopApp:run`), ispezione visiva di Esercizi (StaffPreview), Pratica in modalità Contesto e Spartito, e Scale; passata di tuning sulle costanti (`u`, spessori, pendenza) se qualcosa stona.
