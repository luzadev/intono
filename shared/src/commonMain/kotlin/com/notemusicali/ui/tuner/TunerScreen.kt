package com.notemusicali.ui.tuner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notemusicali.audio.InstrumentPreset
import com.notemusicali.ui.components.BackTopBar
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.notemusicali.ui.components.KeepScreenOn
import com.notemusicali.ui.theme.TunerColors

@Composable
fun TunerScreen(
    onBack: () -> Unit,
    viewModel: TunerViewModel = viewModel { TunerViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    KeepScreenOn()

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (state.tuningState) {
            TunerViewModel.TuningState.IN_TUNE -> TunerColors.inTuneBackground
            TunerViewModel.TuningState.OUT_OF_TUNE -> TunerColors.outOfTuneBackground
            TunerViewModel.TuningState.SILENCE -> TunerColors.silenceBackground
        },
        animationSpec = tween(durationMillis = 300),
        label = "bgColor",
    )

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
        ) {
            BackTopBar(title = stringResource(Res.string.tuner_title), onBack = onBack)

            // Instrument selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InstrumentPreset.entries.forEach { preset ->
                    FilterChip(
                        selected = state.instrument == preset,
                        onClick = { viewModel.selectInstrument(preset) },
                        label = { Text(preset.displayName, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            labelColor = Color.White.copy(alpha = 0.7f),
                            selectedContainerColor = Color.White.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            ) {
                // Note name with glow background
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 48.dp, vertical = 16.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.noteName.isNotEmpty()) state.noteName else "---",
                            fontSize = 96.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )

                        if (state.noteName.isNotEmpty()) {
                            Text(
                                text = stringResource(Res.string.octave_n, state.octave.toString()),
                                fontSize = 24.sp,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Cents gauge
                CentsGauge(
                    cents = state.centsDeviation,
                    isActive = state.tuningState != TunerViewModel.TuningState.SILENCE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Frequency and cents info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (state.frequency > 0f) {
                        Text(
                            text = "${(state.frequency * 10).toInt() / 10.0} Hz",
                            fontSize = 20.sp,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    }

                    if (state.tuningState != TunerViewModel.TuningState.SILENCE && state.frequency > 0f) {
                        Text(
                            text = "  \u00B7  ",
                            fontSize = 20.sp,
                            color = Color.White.copy(alpha = 0.3f),
                        )
                        val sign = if (state.centsDeviation >= 0) "+" else ""
                        Text(
                            text = "${sign}${state.centsDeviation.toInt()} cents",
                            fontSize = 20.sp,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    }
                }
            }
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
        animationSpec = tween(durationMillis = 200),
        label = "gaugeColor",
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val gaugeWidth = size.width * 0.8f
        val gaugeLeft = centerX - gaugeWidth / 2f

        // Background track
        drawLine(
            color = Color.White.copy(alpha = 0.15f),
            start = Offset(gaugeLeft, centerY),
            end = Offset(gaugeLeft + gaugeWidth, centerY),
            strokeWidth = 8f,
            cap = StrokeCap.Round,
        )

        // In-tune zone highlight
        val zoneWidth = gaugeWidth * 15f / 50f
        drawLine(
            color = TunerColors.inTune.copy(alpha = 0.2f),
            start = Offset(centerX - zoneWidth, centerY),
            end = Offset(centerX + zoneWidth, centerY),
            strokeWidth = 8f,
            cap = StrokeCap.Round,
        )

        // Center tick mark
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(centerX, centerY - 20f),
            end = Offset(centerX, centerY + 20f),
            strokeWidth = 2f,
        )

        // Zone boundary markers
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(centerX - zoneWidth, centerY - 12f),
            end = Offset(centerX - zoneWidth, centerY + 12f),
            strokeWidth = 1f,
        )
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(centerX + zoneWidth, centerY - 12f),
            end = Offset(centerX + zoneWidth, centerY + 12f),
            strokeWidth = 1f,
        )

        // Indicator with glow
        if (isActive) {
            val clamped = cents.coerceIn(-50f, 50f)
            val indicatorX = centerX + (clamped / 50f) * (gaugeWidth / 2f)

            // Glow ring
            drawCircle(
                color = indicatorColor.copy(alpha = 0.3f),
                radius = 20f,
                center = Offset(indicatorX, centerY),
            )
            // Main indicator
            drawCircle(
                color = indicatorColor,
                radius = 14f,
                center = Offset(indicatorX, centerY),
            )
            // Inner highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 6f,
                center = Offset(indicatorX - 2f, centerY - 2f),
            )
        }
    }
}
