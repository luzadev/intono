package com.notemusicali.ui.eartraining

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notemusicali.eartraining.EarTrainingDifficulty
import com.notemusicali.eartraining.EarTrainingMode
import com.notemusicali.ui.components.BackTopBar
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun EarTrainingScreen(
    onBack: () -> Unit,
    viewModel: EarTrainingViewModel = viewModel { EarTrainingViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            BackTopBar(title = stringResource(Res.string.ear_training_title), onBack = onBack)

            if (state.isSessionComplete) {
                SessionCompleteView(
                    correct = state.correctCount,
                    total = state.totalQuestions,
                    onRestart = { viewModel.startSession() },
                    onBack = onBack,
                )
            } else if (state.currentQuestion != null) {
                QuestionView(
                    state = state,
                    onPlay = { viewModel.playSound() },
                    onAnswer = { viewModel.submitAnswer(it) },
                )
            } else {
                SetupView(
                    state = state,
                    onModeChange = { viewModel.setMode(it) },
                    onDifficultyChange = { viewModel.setDifficulty(it) },
                    onStart = { viewModel.startSession() },
                )
            }
        }
    }
}

@Composable
private fun SetupView(
    state: EarTrainingUiState,
    onModeChange: (EarTrainingMode) -> Unit,
    onDifficultyChange: (EarTrainingDifficulty) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Mode tabs
        val modeIndex = EarTrainingMode.entries.indexOf(state.mode)
        TabRow(
            selectedTabIndex = modeIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            EarTrainingMode.entries.forEach { mode ->
                Tab(
                    selected = state.mode == mode,
                    onClick = { onModeChange(mode) },
                ) {
                    Text(mode.displayName, modifier = Modifier.padding(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Difficulty
        Text(stringResource(Res.string.difficulty), fontSize = 16.sp, color = Color.White.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EarTrainingDifficulty.entries.forEach { diff ->
                FilterChip(
                    selected = state.difficulty == diff,
                    onClick = { onDifficultyChange(diff) },
                    label = { Text(diff.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        labelColor = Color.White.copy(alpha = 0.7f),
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(0.6f),
        ) {
            Text(stringResource(Res.string.start), fontSize = 18.sp)
        }
    }
}

@Composable
private fun QuestionView(
    state: EarTrainingUiState,
    onPlay: () -> Unit,
    onAnswer: (String) -> Unit,
) {
    val question = state.currentQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Progress
        Text(
            text = stringResource(Res.string.question_n, state.questionNumber, state.totalQuestions),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.5f),
        )

        // Score
        Text(
            text = stringResource(Res.string.correct_count, state.correctCount),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4CAF50),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Play button
        IconButton(
            onClick = onPlay,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    if (state.isPlaying) Color(0xFFFF9800).copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(Res.string.play_sound),
                tint = Color.White,
                modifier = Modifier.size(48.dp),
            )
        }

        Text(
            text = if (state.isPlaying) stringResource(Res.string.listening) else stringResource(Res.string.press_to_listen),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Answer choices (2x2 grid)
        val choices = question.choices
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            for (row in choices.chunked(2)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { choice ->
                        val bgColor = when {
                            state.selectedAnswer == null -> Color.White.copy(alpha = 0.1f)
                            choice == question.correctAnswer -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                            choice == state.selectedAnswer -> Color(0xFFF44336).copy(alpha = 0.3f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }

                        Button(
                            onClick = { onAnswer(choice) },
                            enabled = state.selectedAnswer == null,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = bgColor,
                                disabledContainerColor = bgColor,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = choice,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SessionCompleteView(
    correct: Int,
    total: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = stringResource(Res.string.result),
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFFFD54F),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.session_completed),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$correct/$total",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                correct.toFloat() / total >= 0.8f -> Color(0xFF4CAF50)
                correct.toFloat() / total >= 0.5f -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
        )

        Text(
            text = stringResource(Res.string.correct_answers),
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onRestart) {
                Text(stringResource(Res.string.retry))
            }
            Button(onClick = onBack) {
                Text(stringResource(Res.string.finish))
            }
        }
    }
}
