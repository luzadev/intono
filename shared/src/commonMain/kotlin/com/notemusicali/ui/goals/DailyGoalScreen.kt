package com.notemusicali.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.goals.DailyGoalManager
import com.notemusicali.ui.components.BackTopBar
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyGoalScreen(onBack: () -> Unit) {
    var settings by remember { mutableStateOf(DailyGoalManager.getSettings()) }
    val state = remember { DailyGoalManager.getState() }
    val progress = remember { DailyGoalManager.todayProgressFraction() }
    val goalMet = remember { DailyGoalManager.isTodayGoalMet() }
    val history = remember { DailyGoalManager.recentHistory(30) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            BackTopBar(title = stringResource(Res.string.goals_title), onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Streak display
                if (state.streak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text("\uD83D\uDD25", fontSize = 40.sp) // fire emoji
                        Text(
                            text = "${state.streak}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Text(
                        text = if (state.streak == 1) stringResource(Res.string.consecutive_day_one) else stringResource(Res.string.consecutive_days),
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                } else {
                    Text(
                        stringResource(Res.string.start_streak),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Circular progress for today
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(160.dp),
                        color = if (goalMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.1f),
                        strokeWidth = 12.dp,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val todayMin = (state.todayMs / 60_000).toInt()
                        Text(
                            text = "${todayMin}m",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = stringResource(Res.string.of_target, settings.targetMinutes),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.5f),
                        )
                        if (goalMet) {
                            Text(
                                text = stringResource(Res.string.goal_reached),
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Goal setting
                Text(stringResource(Res.string.daily_goal), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 30).forEach { min ->
                        FilterChip(
                            selected = settings.targetMinutes == min,
                            onClick = {
                                settings = settings.copy(targetMinutes = min)
                                DailyGoalManager.saveSettings(settings)
                            },
                            label = { Text("${min}m") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                labelColor = Color.White.copy(alpha = 0.7f),
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Mini calendar (last 30 days)
                Text(stringResource(Res.string.last_30_days), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                if (history.isEmpty()) {
                    Text(
                        stringResource(Res.string.no_recent_activity),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.4f),
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        history.forEach { (date, minutes) ->
                            val met = minutes >= settings.targetMinutes
                            val intensity = (minutes.toFloat() / settings.targetMinutes).coerceAtMost(1f)
                            val bgColor = if (met) Color(0xFF4CAF50)
                                else if (minutes > 0) Color(0xFF4CAF50).copy(alpha = intensity * 0.5f)
                                else Color.White.copy(alpha = 0.05f)

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(bgColor)
                                    .then(
                                        if (met) Modifier.border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                // Show day number
                                val day = date.takeLast(2)
                                Text(
                                    text = day,
                                    fontSize = 8.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
