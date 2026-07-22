package com.notemusicali.ui.practice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Piano
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.audio.InstrumentPreset
import com.notemusicali.music.ItalianNotation
import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteDuration
import com.notemusicali.music.NoteSequence
import com.notemusicali.ui.components.BackTopBar
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val NoteButtonGradient = Brush.linearGradient(
    listOf(Color(0xFF00695C), Color(0xFF00838F)),
)
private val NoteChipGradient = Brush.linearGradient(
    listOf(Color(0xFF1565C0), Color(0xFF7B1FA2)),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualInputScreen(
    onBack: () -> Unit,
    onSequenceReady: (NoteSequence, InstrumentPreset) -> Unit,
) {
    val notes = remember { mutableStateListOf<MusicalNote>() }
    var selectedInstrument by remember { mutableStateOf(InstrumentPreset.VIOLINO) }
    var selectedOctave by remember { mutableIntStateOf(4) }
    var useFlats by remember { mutableStateOf(false) }
    var selectedDuration by remember { mutableStateOf(NoteDuration.QUARTER) }
    val manualSequenceName = stringResource(Res.string.manual_sequence_name)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
    Column(
        modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight(),
    ) {
        BackTopBar(
            title = stringResource(Res.string.note_input_title),
            onBack = onBack,
            contentColor = MaterialTheme.colorScheme.primary,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {

            // Instrument selector section
            SectionHeader(icon = Icons.Outlined.Piano, label = stringResource(Res.string.instrument))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InstrumentPreset.entries.forEach { preset ->
                    FilterChip(
                        selected = selectedInstrument == preset,
                        onClick = {
                            selectedInstrument = preset
                            if (selectedOctave !in preset.octaveRange) {
                                selectedOctave = preset.octaveRange.first
                                    .coerceAtLeast(3)
                                    .coerceAtMost(preset.octaveRange.last)
                            }
                        },
                        label = { Text(preset.displayName, fontSize = 13.sp) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Octave selector section
            SectionHeader(icon = Icons.Outlined.MusicNote, label = stringResource(Res.string.octave))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (oct in selectedInstrument.octaveRange) {
                    FilterChip(
                        selected = selectedOctave == oct,
                        onClick = { selectedOctave = oct },
                        label = { Text("$oct") },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sharp/Flat toggle
            Row {
                FilterChip(
                    selected = !useFlats,
                    onClick = { useFlats = false },
                    label = { Text("#") },
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = useFlats,
                    onClick = { useFlats = true },
                    label = { Text("b") },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration selector
            SectionHeader(icon = Icons.Outlined.MusicNote, label = stringResource(Res.string.duration))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NoteDuration.entries.forEach { dur ->
                    FilterChip(
                        selected = selectedDuration == dur,
                        onClick = { selectedDuration = dur },
                        label = {
                            NoteDurationIcon(
                                duration = dur,
                                color = MaterialTheme.colorScheme.onSurface,
                                size = 28.dp,
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note buttons grid
            SectionHeader(icon = Icons.Outlined.Edit, label = stringResource(Res.string.notes))
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ItalianNotation.allNotes().forEachIndexed { index, noteName ->
                    val displayText = if (useFlats && noteName.flat != null) {
                        noteName.flat
                    } else {
                        noteName.sharp
                    }
                    NoteButton(
                        text = displayText,
                        onClick = {
                            notes.add(
                                MusicalNote.fromNameAndOctave(
                                    index, selectedOctave, selectedDuration,
                                    preferFlat = useFlats && noteName.flat != null,
                                ),
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected notes preview
            Text(
                text = stringResource(Res.string.selected_notes, notes.size),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )

            if (notes.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp),
                        )
                        .padding(12.dp),
                ) {
                    items(notes.toList()) { note ->
                        val chipShape = RoundedCornerShape(8.dp)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(chipShape)
                                .background(NoteChipGradient)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), chipShape)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = note.fullName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                            )
                            NoteDurationIcon(
                                duration = note.duration,
                                color = Color.White.copy(alpha = 0.6f),
                                size = 14.dp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        if (notes.isNotEmpty()) notes.removeLast()
                    },
                    enabled = notes.isNotEmpty(),
                ) {
                    Text(stringResource(Res.string.undo_last))
                }

                OutlinedButton(
                    onClick = { notes.clear() },
                    enabled = notes.isNotEmpty(),
                ) {
                    Text(stringResource(Res.string.clear_all))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onSequenceReady(
                            NoteSequence(name = manualSequenceName, notes = notes.toList()),
                            selectedInstrument,
                        )
                    },
                    enabled = notes.size >= 2,
                ) {
                    Text(
                        text = stringResource(Res.string.start_practice_n, notes.size),
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
    }
}

// ── Note duration icon drawn with Canvas ──────────────────────────

@Composable
private fun NoteDurationIcon(
    duration: NoteDuration,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        when (duration) {
            NoteDuration.WHOLE -> drawWholeNote(color, w, h)
            NoteDuration.HALF -> drawHalfNote(color, w, h)
            NoteDuration.QUARTER -> drawQuarterNote(color, w, h)
            NoteDuration.EIGHTH -> drawNoteWithFlags(color, w, h, flags = 1)
            NoteDuration.SIXTEENTH -> drawNoteWithFlags(color, w, h, flags = 2)
            NoteDuration.THIRTY_SECOND -> drawNoteWithFlags(color, w, h, flags = 3)
        }
    }
}

/** Whole note: open oval, no stem. */
private fun DrawScope.drawWholeNote(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val cy = h * 0.55f
    val rx = w * 0.30f
    val ry = h * 0.18f
    rotate(degrees = -15f, pivot = Offset(cx, cy)) {
        drawOval(
            color = color,
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2, ry * 2),
            style = Stroke(width = w * 0.06f),
        )
    }
}

/** Half note: open oval + stem. */
private fun DrawScope.drawHalfNote(color: Color, w: Float, h: Float) {
    val cx = w * 0.42f
    val cy = h * 0.72f
    val rx = w * 0.22f
    val ry = h * 0.13f
    // Oval (open)
    rotate(degrees = -15f, pivot = Offset(cx, cy)) {
        drawOval(
            color = color,
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2, ry * 2),
            style = Stroke(width = w * 0.06f),
        )
    }
    // Stem
    val stemX = cx + rx * 0.92f
    drawLine(
        color = color,
        start = Offset(stemX, cy),
        end = Offset(stemX, h * 0.12f),
        strokeWidth = w * 0.06f,
        cap = StrokeCap.Round,
    )
}

/** Quarter note: filled oval + stem. */
private fun DrawScope.drawQuarterNote(color: Color, w: Float, h: Float) {
    val cx = w * 0.42f
    val cy = h * 0.72f
    val rx = w * 0.22f
    val ry = h * 0.13f
    // Oval (filled)
    rotate(degrees = -15f, pivot = Offset(cx, cy)) {
        drawOval(
            color = color,
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2, ry * 2),
            style = Fill,
        )
    }
    // Stem
    val stemX = cx + rx * 0.92f
    drawLine(
        color = color,
        start = Offset(stemX, cy),
        end = Offset(stemX, h * 0.12f),
        strokeWidth = w * 0.06f,
        cap = StrokeCap.Round,
    )
}

/** Eighth / sixteenth / thirty-second: filled oval + stem + flags. */
private fun DrawScope.drawNoteWithFlags(color: Color, w: Float, h: Float, flags: Int) {
    val cx = w * 0.38f
    val cy = h * 0.75f
    val rx = w * 0.20f
    val ry = h * 0.12f
    // Oval (filled)
    rotate(degrees = -15f, pivot = Offset(cx, cy)) {
        drawOval(
            color = color,
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2, ry * 2),
            style = Fill,
        )
    }
    // Stem
    val stemX = cx + rx * 0.92f
    val stemTop = h * 0.10f
    drawLine(
        color = color,
        start = Offset(stemX, cy),
        end = Offset(stemX, stemTop),
        strokeWidth = w * 0.06f,
        cap = StrokeCap.Round,
    )
    // Flags
    val flagSpacing = h * 0.13f
    for (i in 0 until flags) {
        val flagTop = stemTop + i * flagSpacing
        val path = Path().apply {
            moveTo(stemX, flagTop)
            cubicTo(
                stemX + w * 0.25f, flagTop + h * 0.06f,
                stemX + w * 0.30f, flagTop + h * 0.12f,
                stemX + w * 0.10f, flagTop + h * 0.18f,
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round),
        )
    }
}

// ── Other composables ─────────────────────────────────────────────

@Composable
private fun SectionHeader(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun NoteButton(
    text: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(shape)
            .background(NoteButtonGradient)
            .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
        )
    }
}
