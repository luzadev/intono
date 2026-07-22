package com.notemusicali.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.challenge.ChallengeRepository
import com.notemusicali.challenge.ChallengeResult
import com.notemusicali.eartraining.EarTrainingSession
import com.notemusicali.eartraining.EarTrainingStatsRepository
import com.notemusicali.history.PracticeSession
import com.notemusicali.history.SessionRepository
import com.notemusicali.ui.components.BackTopBar
import com.notemusicali.ui.components.GradientCard
import com.notemusicali.ui.theme.CardAccents
import com.notemusicali.ui.theme.CardGradients
import com.notemusicali.util.formatEpochDate
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Unified history item wrapping different session types. */
private sealed class HistoryItem(val dateMillis: Long) {
    class Practice(val session: PracticeSession) : HistoryItem(session.dateMillis)
    class EarTraining(val session: EarTrainingSession) : HistoryItem(session.dateMillis)
    class Challenge(val result: ChallengeResult) : HistoryItem(result.dateMillis)
}

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onReplay: (PracticeSession) -> Unit,
) {
    val allItems = remember {
        val practice = SessionRepository.load().map { HistoryItem.Practice(it) }
        val earTraining = EarTrainingStatsRepository.load().map { HistoryItem.EarTraining(it) }
        val challenges = ChallengeRepository.load().map { HistoryItem.Challenge(it) }
        (practice + earTraining + challenges).sortedByDescending { it.dateMillis }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight()) {
            BackTopBar(
                title = stringResource(Res.string.history_title),
                onBack = onBack,
                contentColor = MaterialTheme.colorScheme.primary,
            )

            if (allItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.no_sessions),
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    items(allItems) { item ->
                        when (item) {
                            is HistoryItem.Practice -> PracticeCard(
                                session = item.session,
                                onClick = { onReplay(item.session) },
                            )
                            is HistoryItem.EarTraining -> EarTrainingCard(
                                session = item.session,
                            )
                            is HistoryItem.Challenge -> ChallengeCard(
                                result = item.result,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Practice session card ─────────────────────────────────────────

@Composable
private fun PracticeCard(session: PracticeSession, onClick: () -> Unit) {
    GradientCard(
        gradient = CardGradients.practice,
        onClick = onClick,
        accentColor = CardAccents.practice,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.sequenceName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    text = session.instrumentName,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
            TypeBadge(
                text = stringResource(Res.string.history_type_practice),
                color = CardAccents.practice,
            )
        }

        if (!session.completed) {
            Text(
                text = stringResource(Res.string.partial),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFFB74D),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val noteLabel = if (session.completed) {
                "${session.noteResults.size} note"
            } else {
                "${session.noteResults.size}/${session.noteMidiNumbers.size} note"
            }
            InfoChip(label = noteLabel)
            InfoChip(label = "~${session.averageCents.toInt()} cents")
            InfoChip(label = "${(session.firstAttemptRate * 100).toInt()}% 1\u00B0")

            val totalSec = session.totalTimeMs / 1000
            InfoChip(label = "${totalSec / 60}m ${totalSec % 60}s")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatEpochDate(session.dateMillis),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.4f),
        )
    }
}

// ── Ear Training session card ─────────────────────────────────────

@Composable
private fun EarTrainingCard(session: EarTrainingSession) {
    val modeLabel = when (session.mode) {
        "NOTE" -> stringResource(Res.string.history_ear_note)
        else -> stringResource(Res.string.history_ear_interval)
    }
    val diffLabel = when (session.difficulty) {
        "EASY" -> "Easy"
        "MEDIUM" -> "Medium"
        else -> "Hard"
    }
    val pct = (session.accuracy * 100).toInt()
    val scoreColor = when {
        pct >= 80 -> Color(0xFF4CAF50)
        pct >= 50 -> Color(0xFFFFA726)
        else -> Color(0xFFEF5350)
    }

    GradientCard(
        gradient = CardGradients.earTraining,
        onClick = {},
        accentColor = CardAccents.earTraining,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$modeLabel — $diffLabel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
            TypeBadge(
                text = stringResource(Res.string.history_type_ear_training),
                color = CardAccents.earTraining,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            InfoChip(
                label = "${session.correctAnswers}/${session.totalQuestions} ${stringResource(Res.string.history_correct)}",
            )
            Text(
                text = "$pct%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatEpochDate(session.dateMillis),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.4f),
        )
    }
}

// ── Challenge result card ─────────────────────────────────────────

@Composable
private fun ChallengeCard(result: ChallengeResult) {
    GradientCard(
        gradient = CardGradients.challenge,
        onClick = {},
        accentColor = CardAccents.challenge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.sequenceName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    text = "${result.timeLimitSec}s",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
            TypeBadge(
                text = stringResource(Res.string.history_type_challenge),
                color = CardAccents.challenge,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InfoChip(label = "${result.notesPlayed} note")
            InfoChip(label = "${result.maxCombo}x ${stringResource(Res.string.history_combo)}")
            Text(
                text = "${result.totalScore} pt",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CardAccents.challenge,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatEpochDate(result.dateMillis),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.4f),
        )
    }
}

// ── Shared UI pieces ──────────────────────────────────────────────

@Composable
private fun TypeBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun InfoChip(label: String) {
    Text(
        text = label,
        fontSize = 12.sp,
        color = Color.White.copy(alpha = 0.7f),
    )
}
