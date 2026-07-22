package com.notemusicali.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Piano
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SpeakerPhone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.ui.components.GradientCard
import com.notemusicali.ui.theme.AppColors
import com.notemusicali.ui.theme.CardAccents
import com.notemusicali.ui.theme.CardGradients
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    onNavigateToTuner: () -> Unit,
    onNavigateToPractice: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToScores: () -> Unit = {},
    onNavigateToMetronome: () -> Unit = {},
    onNavigateToReference: () -> Unit = {},
    onNavigateToScales: () -> Unit = {},
    onNavigateToEarTraining: () -> Unit = {},
    onNavigateToChallenge: () -> Unit = {},
    onNavigateToLeaderboard: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToDailyGoals: () -> Unit = {},
    onNavigateToGuide: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo: "In" white + "Tono" gold (like Remotion video)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) {
                        append("In")
                    }
                    withStyle(SpanStyle(color = AppColors.accent)) {
                        append("Tono")
                    }
                },
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
            )

            Text(
                text = stringResource(Res.string.app_subtitle),
                fontSize = 14.sp,
                color = AppColors.textMuted,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Gold decorative line
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, AppColors.accent, Color.Transparent),
                        ),
                    ),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Strumenti ---
            SectionHeader(stringResource(Res.string.section_tools))

            HomeCard(
                icon = Icons.Outlined.MusicNote,
                title = stringResource(Res.string.tuner_title),
                description = stringResource(Res.string.tuner_desc),
                gradient = CardGradients.tuner,
                accentColor = CardAccents.tuner,
                onClick = onNavigateToTuner,
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeCard(
                icon = Icons.Outlined.Speed,
                title = stringResource(Res.string.metronome_title),
                description = stringResource(Res.string.metronome_desc),
                gradient = CardGradients.metronome,
                accentColor = CardAccents.metronome,
                onClick = onNavigateToMetronome,
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeCard(
                icon = Icons.Outlined.SpeakerPhone,
                title = stringResource(Res.string.reference_title),
                description = stringResource(Res.string.reference_desc),
                gradient = CardGradients.reference,
                accentColor = CardAccents.reference,
                onClick = onNavigateToReference,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Pratica ---
            SectionHeader(stringResource(Res.string.section_practice))

            HomeCard(
                icon = Icons.Outlined.School,
                title = stringResource(Res.string.practice_title),
                description = stringResource(Res.string.practice_desc),
                gradient = CardGradients.practice,
                accentColor = CardAccents.practice,
                onClick = onNavigateToPractice,
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeCard(
                icon = Icons.AutoMirrored.Outlined.List,
                title = stringResource(Res.string.exercises_title),
                description = stringResource(Res.string.exercises_desc),
                gradient = CardGradients.exercises,
                accentColor = CardAccents.exercises,
                onClick = onNavigateToExercises,
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeCard(
                icon = Icons.Outlined.Piano,
                title = stringResource(Res.string.scales_title),
                description = stringResource(Res.string.scales_desc),
                gradient = CardGradients.scales,
                accentColor = CardAccents.scales,
                onClick = onNavigateToScales,
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeCard(
                icon = Icons.Outlined.LibraryMusic,
                title = stringResource(Res.string.scores_title),
                description = stringResource(Res.string.scores_desc),
                gradient = CardGradients.scores,
                accentColor = CardAccents.scores,
                onClick = onNavigateToScores,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Sfida ---
            SectionHeader(stringResource(Res.string.section_challenge))

            HomeCard(
                icon = Icons.Outlined.Hearing,
                title = stringResource(Res.string.ear_training_title),
                description = stringResource(Res.string.ear_training_desc),
                gradient = CardGradients.earTraining,
                accentColor = CardAccents.earTraining,
                onClick = onNavigateToEarTraining,
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeCard(
                icon = Icons.Outlined.EmojiEvents,
                title = stringResource(Res.string.challenge_title),
                description = stringResource(Res.string.challenge_desc),
                gradient = CardGradients.challenge,
                accentColor = CardAccents.challenge,
                onClick = onNavigateToChallenge,
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeCard(
                icon = Icons.Outlined.Leaderboard,
                title = stringResource(Res.string.leaderboard_title),
                description = stringResource(Res.string.leaderboard_desc),
                gradient = CardGradients.leaderboard,
                accentColor = CardAccents.leaderboard,
                onClick = onNavigateToLeaderboard,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Progressi ---
            SectionHeader(stringResource(Res.string.section_progress))

            HomeCard(
                icon = Icons.Outlined.BarChart,
                title = stringResource(Res.string.stats_title),
                description = stringResource(Res.string.stats_desc),
                gradient = CardGradients.stats,
                accentColor = CardAccents.stats,
                onClick = onNavigateToStats,
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeCard(
                icon = Icons.Outlined.Flag,
                title = stringResource(Res.string.goals_title),
                description = stringResource(Res.string.goals_desc),
                gradient = CardGradients.goals,
                accentColor = CardAccents.goals,
                onClick = onNavigateToDailyGoals,
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeCard(
                icon = Icons.Outlined.History,
                title = stringResource(Res.string.history_title),
                description = stringResource(Res.string.history_desc),
                gradient = CardGradients.history,
                accentColor = CardAccents.history,
                onClick = onNavigateToHistory,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Guida ---
            HomeCard(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                title = stringResource(Res.string.guide_title),
                description = stringResource(Res.string.guide_desc),
                gradient = CardGradients.guide,
                accentColor = CardAccents.guide,
                onClick = onNavigateToGuide,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.accent.copy(alpha = 0.6f),
        letterSpacing = 3.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    )
}

@Composable
private fun HomeCard(
    icon: ImageVector,
    title: String,
    description: String,
    gradient: Brush,
    accentColor: Color,
    onClick: () -> Unit,
) {
    GradientCard(
        gradient = gradient,
        onClick = onClick,
        accentColor = accentColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = accentColor.copy(alpha = 0.9f),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }
        }
    }
}
