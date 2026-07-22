package com.notemusicali.ui.challenge

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.ui.components.BackTopBar
import com.notemusicali.ui.components.KeepScreenOn
import com.notemusicali.ui.components.StaffView
import com.notemusicali.ui.theme.TunerColors
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChallengeScreen(
    onBack: () -> Unit,
    viewModel: ChallengeViewModel,
) {
    val state by viewModel.uiState.collectAsState()

    KeepScreenOn()

    DisposableEffect(Unit) {
        onDispose { /* cleanup handled in ViewModel */ }
    }

    if (state.isComplete) {
        ChallengeCompleteView(
            state = state,
            onBack = onBack,
        )
        return
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (state.isCorrect) TunerColors.inTuneBackground
            else TunerColors.silenceBackground,
        animationSpec = tween(200),
        label = "bgColor",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BackTopBar(title = stringResource(Res.string.challenge_title), onBack = onBack)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Timer bar
                    val timeProgress = state.remainingMs.toFloat() / (state.timeLimit.seconds * 1000f)
                    LinearProgressIndicator(
                        progress = { timeProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            timeProgress > 0.5f -> Color(0xFF4CAF50)
                            timeProgress > 0.2f -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        },
                        trackColor = Color.White.copy(alpha = 0.15f),
                    )

                    // Timer text
                    val sec = state.remainingMs / 1000
                    Text(
                        text = "${sec}s",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Score + Combo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(Res.string.score), fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                            Text(
                                text = "${state.score}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(Res.string.combo), fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                            Text(
                                text = "${state.combo}x",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.combo > 0) Color(0xFFFF9800) else Color.White.copy(alpha = 0.4f),
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(Res.string.notes), fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                            Text(
                                text = "${state.notesPlayed}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Target note on staff
                    state.targetNote?.let { note ->
                        StaffView(
                            note = note,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    // Target note name
                    Text(
                        text = state.targetNote?.displayName ?: "",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = stringResource(Res.string.octave_n, (state.targetNote?.octave ?: "").toString()),
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Detected note
                    if (state.detectedNoteName.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = state.detectedNoteName,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (state.isCorrect) TunerColors.inTune else TunerColors.outOfTune,
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
private fun ChallengeCompleteView(
    state: ChallengeUiState,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A237E), Color(0xFF0D1B47)),
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
                contentDescription = stringResource(Res.string.challenge_completed),
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFFFD54F),
            )

            Text(
                text = stringResource(Res.string.challenge_completed),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Text(
                text = "${state.score}",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F),
            )
            Text(
                text = stringResource(Res.string.points),
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stringResource(Res.string.notes_played), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${state.notesPlayed}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stringResource(Res.string.max_combo), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${state.maxCombo}x", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stringResource(Res.string.time), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${state.timeLimit.displayName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack) {
                Text(stringResource(Res.string.finish))
            }
        }
    }
}
