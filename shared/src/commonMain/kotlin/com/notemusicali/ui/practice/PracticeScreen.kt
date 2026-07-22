package com.notemusicali.ui.practice

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notemusicali.history.PracticeSession
import com.notemusicali.metronome.MetronomeEngine
import com.notemusicali.ui.components.BackTopBar
import com.notemusicali.ui.components.KeepScreenOn
import com.notemusicali.ui.components.StaffContextView
import com.notemusicali.ui.components.StaffFullView
import com.notemusicali.ui.components.StaffView
import com.notemusicali.ui.theme.TunerColors
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private enum class StaffMode {
    SINGLE,
    CONTEXT,
    FULL,
}

@Composable
fun PracticeScreen(
    onBack: () -> Unit,
    viewModel: PracticeViewModel = viewModel { PracticeViewModel() },
    onFinished: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var staffMode by rememberSaveable { mutableStateOf(StaffMode.SINGLE.name) }
    val currentMode = StaffMode.valueOf(staffMode)
    var lightStaff by rememberSaveable { mutableStateOf(false) }
    var hideNoteName by rememberSaveable { mutableStateOf(false) }

    // Integrated metronome
    val metronomeEngine = remember { MetronomeEngine() }
    var metronomeOn by rememberSaveable { mutableStateOf(false) }
    var metronomeBpm by rememberSaveable { mutableIntStateOf(120) }

    KeepScreenOn()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.savePartialSession()
            viewModel.stopListening()
            metronomeEngine.release()
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (state.matchState) {
            PracticeViewModel.NoteMatchState.CORRECT -> TunerColors.inTuneBackground
            PracticeViewModel.NoteMatchState.INCORRECT -> TunerColors.outOfTuneBackground
            PracticeViewModel.NoteMatchState.WAITING -> TunerColors.silenceBackground
        },
        animationSpec = tween(durationMillis = 300),
        label = "bgColor",
    )

    if (state.isCompleted) {
        LaunchedEffect(Unit) {
            metronomeOn = false
            metronomeEngine.stop()
        }
        CompletedView(
            session = state.lastSession,
            onSave = { viewModel.saveSession() },
            onFinished = onFinished,
            onReset = { viewModel.reset() },
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                )
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BackTopBar(title = stringResource(Res.string.practice_title), onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Progress bar
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = TunerColors.inTune,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )

                Text(
                    text = stringResource(Res.string.note_n_of_total, state.currentIndex + 1, state.sequence.size),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 8.dp),
                )

                // View mode selector
                Row(
                    modifier = Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StaffMode.entries.forEach { mode ->
                        val modeLabel = when (mode) {
                            StaffMode.SINGLE -> stringResource(Res.string.view_note)
                            StaffMode.CONTEXT -> stringResource(Res.string.view_context)
                            StaffMode.FULL -> stringResource(Res.string.view_score)
                        }
                        FilterChip(
                            selected = currentMode == mode,
                            onClick = { staffMode = mode.name },
                            label = { Text(modeLabel, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                labelColor = Color.White.copy(alpha = 0.7f),
                                selectedContainerColor = Color.White.copy(alpha = 0.25f),
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                    FilterChip(
                        selected = lightStaff,
                        onClick = { lightStaff = !lightStaff },
                        label = { Text(stringResource(Res.string.view_light), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            labelColor = Color.White.copy(alpha = 0.7f),
                            selectedContainerColor = Color.White.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White,
                        ),
                    )
                    FilterChip(
                        selected = hideNoteName,
                        onClick = { hideNoteName = !hideNoteName },
                        label = { Text(stringResource(Res.string.hide_note_name), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            labelColor = Color.White.copy(alpha = 0.7f),
                            selectedContainerColor = Color(0xFFFF6B9D).copy(alpha = 0.3f),
                            selectedLabelColor = Color(0xFFFF6B9D),
                        ),
                    )
                }

                // Mini metronome bar
                val scope = rememberCoroutineScope()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            metronomeOn = !metronomeOn
                            if (metronomeOn) {
                                metronomeEngine.bpm = metronomeBpm
                                metronomeEngine.start(scope)
                            } else {
                                metronomeEngine.stop()
                            }
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = if (metronomeOn) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = if (metronomeOn) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Text(
                        text = "$metronomeBpm",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (metronomeOn) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.widthIn(min = 32.dp),
                    )

                    Slider(
                        value = metronomeBpm.toFloat(),
                        onValueChange = {
                            metronomeBpm = it.toInt()
                            metronomeEngine.bpm = metronomeBpm
                        },
                        valueRange = 30f..240f,
                        modifier = Modifier.weight(1f).height(24.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = if (metronomeOn) Color(0xFFFFD700) else Color.White.copy(alpha = 0.4f),
                            activeTrackColor = if (metronomeOn) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f),
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f),
                        ),
                    )

                    Text(
                        text = "BPM",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Staff display based on mode
                val staffBgMod = if (lightStaff) {
                    Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White)
                } else Modifier

                when (currentMode) {
                    StaffMode.SINGLE -> {
                        Text(
                            text = stringResource(Res.string.play_note),
                            fontSize = 20.sp,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                        state.targetNote?.let { note ->
                            StaffView(
                                note = note,
                                modifier = Modifier.padding(horizontal = 16.dp).then(staffBgMod),
                                lightTheme = lightStaff,
                            )
                        }
                    }
                    StaffMode.CONTEXT -> {
                        StaffContextView(
                            notes = state.sequence.notes,
                            currentIndex = state.currentIndex,
                            modifier = Modifier.padding(horizontal = 8.dp).then(staffBgMod),
                            lightTheme = lightStaff,
                            beats = state.sequence.beats,
                            beatType = state.sequence.beatType,
                        )
                    }
                    StaffMode.FULL -> {
                        StaffFullView(
                            notes = state.sequence.notes,
                            currentIndex = state.currentIndex,
                            // Lo spartito si prende lo spazio residuo: cresce con la finestra
                            modifier = Modifier.weight(1f, fill = false).then(staffBgMod),
                            lightTheme = lightStaff,
                            beats = state.sequence.beats,
                            beatType = state.sequence.beatType,
                        )
                    }
                }

                // Note name with glow card
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (hideNoteName) Color.White.copy(alpha = 0.04f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                        .padding(horizontal = 40.dp, vertical = 8.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (hideNoteName) "?" else (state.targetNote?.displayName ?: ""),
                            fontSize = if (currentMode == StaffMode.SINGLE) 96.sp else 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hideNoteName) Color.White.copy(alpha = 0.15f) else Color.White,
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = if (hideNoteName) "" else stringResource(Res.string.octave_n, (state.targetNote?.octave ?: "").toString()),
                            fontSize = if (currentMode == StaffMode.SINGLE) 20.sp else 16.sp,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detected note card — always reserve space to prevent layout jumps
                val hasDetection = state.detectedNoteName.isNotEmpty()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (hasDetection) Color.White.copy(alpha = 0.06f)
                            else Color.Transparent
                        )
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (hasDetection) stringResource(Res.string.detected) else "",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.4f),
                        )
                        Text(
                            text = if (hasDetection) state.detectedNoteName else " ",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (hasDetection) {
                                when (state.matchState) {
                                    PracticeViewModel.NoteMatchState.CORRECT -> TunerColors.inTune
                                    PracticeViewModel.NoteMatchState.INCORRECT -> TunerColors.outOfTune
                                    PracticeViewModel.NoteMatchState.WAITING -> Color.White
                                }
                            } else Color.Transparent,
                        )

                        val sign = if (state.detectedCents >= 0) "+" else ""
                        Text(
                            text = if (hasDetection) "${sign}${state.detectedCents.toInt()} cents" else "",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.4f),
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
        }
    }
}

@Composable
private fun CompletedView(
    session: PracticeSession?,
    onSave: () -> Unit,
    onFinished: () -> Unit,
    onReset: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onSave()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1B5E20), Color(0xFF0A280E)),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = stringResource(Res.string.completed),
                modifier = Modifier.size(72.dp),
                tint = Color(0xFFFFD54F),
            )

            Text(
                text = stringResource(Res.string.completed),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Text(
                text = stringResource(Res.string.all_notes_correct),
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.7f),
            )

            // Session stats
            if (session != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatRow(
                            label = stringResource(Res.string.avg_deviation),
                            value = "${session.averageCents.toInt()} cents",
                        )
                        StatRow(
                            label = stringResource(Res.string.perfect_notes),
                            value = "${session.perfectNotes}/${session.noteResults.size}",
                        )
                        StatRow(
                            label = stringResource(Res.string.first_attempt),
                            value = "${(session.firstAttemptRate * 100).toInt()}%",
                        )
                        val totalSec = session.totalTimeMs / 1000
                        StatRow(
                            label = stringResource(Res.string.duration),
                            value = "${totalSec / 60}m ${totalSec % 60}s",
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(onClick = onReset) {
                    Text(stringResource(Res.string.repeat))
                }
                Button(onClick = onFinished) {
                    Text(stringResource(Res.string.finish))
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}
