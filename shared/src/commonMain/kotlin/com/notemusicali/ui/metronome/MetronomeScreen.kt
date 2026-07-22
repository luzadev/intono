package com.notemusicali.ui.metronome

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notemusicali.metronome.TimeSignature
import com.notemusicali.ui.components.BackTopBar
import com.notemusicali.ui.components.KeepScreenOn
import com.notemusicali.ui.theme.TunerColors
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun MetronomeScreen(
    onBack: () -> Unit,
    viewModel: MetronomeViewModel = viewModel { MetronomeViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isPlaying || state.tunerActive) {
        KeepScreenOn()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopTuner() }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BackTopBar(title = stringResource(Res.string.metronome_title), onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // BPM display
                Text(
                    text = "${state.bpm}",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(Res.string.bpm),
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // BPM slider
                Slider(
                    value = state.bpm.toFloat(),
                    onValueChange = { viewModel.setBpm(it.toInt()) },
                    valueRange = 30f..240f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                    ),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Time signature
                Text(stringResource(Res.string.time_signature), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeSignature.entries.forEach { ts ->
                        FilterChip(
                            selected = state.timeSignature == ts,
                            onClick = { viewModel.setTimeSignature(ts) },
                            label = { Text(ts.displayName) },
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

                // Beat indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 16.dp),
                ) {
                    repeat(state.timeSignature.beatsPerMeasure) { beat ->
                        val isActive = state.isPlaying && state.currentBeat == beat
                        val isAccent = beat == 0
                        Box(
                            modifier = Modifier
                                .size(if (isAccent) 28.dp else 22.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isActive && isAccent -> Color(0xFFFF9800)
                                        isActive -> MaterialTheme.colorScheme.primary
                                        else -> Color.White.copy(alpha = 0.15f)
                                    }
                                ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Play/Stop + Tap Tempo row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = { viewModel.tapTempo() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                        ),
                    ) {
                        Text(stringResource(Res.string.tap_tempo), color = Color.White)
                    }

                    IconButton(
                        onClick = { viewModel.togglePlayStop() },
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.isPlaying) Color(0xFFF44336).copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = if (state.isPlaying) "Stop" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp),
                        )
                    }

                    // Tuner toggle
                    IconButton(
                        onClick = {
                            if (state.tunerActive) viewModel.stopTuner() else viewModel.startTuner()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.tunerActive) Color(0xFF4CAF50).copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.1f)
                            ),
                    ) {
                        Icon(
                            imageVector = if (state.tunerActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                            contentDescription = "Tuner",
                            tint = if (state.tunerActive) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tuner display
                if (state.tunerActive) {
                    val tunerBgColor by animateColorAsState(
                        targetValue = when (state.tuningDisplay) {
                            TuningDisplay.IN_TUNE -> TunerColors.inTuneBackground
                            TuningDisplay.OUT_OF_TUNE -> TunerColors.outOfTuneBackground
                            TuningDisplay.SILENCE -> Color.White.copy(alpha = 0.04f)
                        },
                        animationSpec = tween(300),
                        label = "tunerBg",
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(tunerBgColor)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Note name + octave
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = state.noteName.ifEmpty { "---" },
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                if (state.noteName.isNotEmpty()) {
                                    Text(
                                        text = "${state.octave}",
                                        fontSize = 20.sp,
                                        color = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                                    )
                                }
                            }

                            // Cents gauge
                            val isActive = state.tuningDisplay != TuningDisplay.SILENCE
                            CentsGauge(
                                cents = state.centsDeviation,
                                isActive = isActive,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                            )

                            // Frequency + cents text
                            if (state.frequency > 0f) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(top = 4.dp),
                                ) {
                                    Text(
                                        text = "${(state.frequency * 10).toInt() / 10.0} Hz",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.5f),
                                    )
                                    if (isActive) {
                                        val sign = if (state.centsDeviation >= 0) "+" else ""
                                        Text(
                                            text = "  ·  ${sign}${state.centsDeviation.toInt()} cents",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.5f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CentsGauge(
    cents: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val indicatorColor by animateColorAsState(
        targetValue = when {
            !isActive -> TunerColors.silence
            kotlin.math.abs(cents) <= 15f -> TunerColors.inTune
            else -> TunerColors.outOfTune
        },
        animationSpec = tween(200),
        label = "gaugeColor",
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val gaugeWidth = size.width * 0.85f
        val gaugeLeft = centerX - gaugeWidth / 2f

        // Background track
        drawLine(
            color = Color.White.copy(alpha = 0.15f),
            start = Offset(gaugeLeft, centerY),
            end = Offset(gaugeLeft + gaugeWidth, centerY),
            strokeWidth = 6f,
            cap = StrokeCap.Round,
        )

        // In-tune zone
        val zoneWidth = gaugeWidth * 15f / 50f
        drawLine(
            color = TunerColors.inTune.copy(alpha = 0.2f),
            start = Offset(centerX - zoneWidth, centerY),
            end = Offset(centerX + zoneWidth, centerY),
            strokeWidth = 6f,
            cap = StrokeCap.Round,
        )

        // Center tick
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(centerX, centerY - 14f),
            end = Offset(centerX, centerY + 14f),
            strokeWidth = 2f,
        )

        // Indicator
        if (isActive) {
            val clamped = cents.coerceIn(-50f, 50f)
            val indicatorX = centerX + (clamped / 50f) * (gaugeWidth / 2f)

            drawCircle(
                color = indicatorColor.copy(alpha = 0.3f),
                radius = 16f,
                center = Offset(indicatorX, centerY),
            )
            drawCircle(
                color = indicatorColor,
                radius = 10f,
                center = Offset(indicatorX, centerY),
            )
        }
    }
}
