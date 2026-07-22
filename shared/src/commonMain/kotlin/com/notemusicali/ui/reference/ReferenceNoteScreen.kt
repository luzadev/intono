package com.notemusicali.ui.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.audio.NoteConverter
import com.notemusicali.audio.ToneGenerator
import com.notemusicali.audio.createToneGenerator
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.notemusicali.music.ItalianNotation
import com.notemusicali.ui.components.BackTopBar

private val NOTE_NAMES = (0..11).map { ItalianNotation.fromNoteIndex(it) }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReferenceNoteScreen(onBack: () -> Unit) {
    val toneGenerator = remember { createToneGenerator() }
    var selectedNoteIndex by remember { mutableIntStateOf(9) } // A (La)
    var selectedOctave by remember { mutableIntStateOf(4) }
    var isPlaying by remember { mutableStateOf(false) }

    val midiNumber = (selectedOctave + 1) * 12 + selectedNoteIndex
    val frequency = NoteConverter.midiToFrequency(midiNumber)
    val noteName = NOTE_NAMES[selectedNoteIndex].display

    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }

    fun playNote(freq: Float = NoteConverter.midiToFrequency((selectedOctave + 1) * 12 + selectedNoteIndex)) {
        toneGenerator.playTone(freq, 3000L)
        isPlaying = true
    }

    fun stopNote() {
        toneGenerator.stop()
        isPlaying = false
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            BackTopBar(title = stringResource(Res.string.reference_title), onBack = {
                stopNote()
                onBack()
            })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Quick access: A4 440Hz
                Button(
                    onClick = {
                        selectedNoteIndex = 9
                        selectedOctave = 4
                        toneGenerator.playTone(440f, 3000L)
                        isPlaying = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.2f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.la_440), fontSize = 18.sp, color = Color(0xFFFF9800))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Note display
                Text(
                    text = noteName,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(Res.string.octave_freq, selectedOctave, ((frequency * 10).toInt() / 10f).toString()),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Play/Stop button
                IconButton(
                    onClick = { if (isPlaying) stopNote() else playNote() },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPlaying) Color(0xFFF44336).copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Note selector
                Text(stringResource(Res.string.select_note), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    NOTE_NAMES.forEachIndexed { idx, name ->
                        FilterChip(
                            selected = selectedNoteIndex == idx,
                            onClick = {
                                selectedNoteIndex = idx
                                if (isPlaying) playNote()
                            },
                            label = { Text(name.display, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                labelColor = Color.White.copy(alpha = 0.7f),
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Octave selector
                Text(stringResource(Res.string.octave), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (2..7).forEach { oct ->
                        FilterChip(
                            selected = selectedOctave == oct,
                            onClick = {
                                selectedOctave = oct
                                if (isPlaying) playNote()
                            },
                            label = { Text("$oct") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                labelColor = Color.White.copy(alpha = 0.7f),
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
