package com.notemusicali.ui.stats

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.history.SessionRepository
import com.notemusicali.stats.StatsCalculator
import com.notemusicali.ui.components.BackTopBar
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun StatsScreen(onBack: () -> Unit) {
    val sessions = remember { SessionRepository.load() }
    val overall = remember(sessions) { StatsCalculator.calculateOverall(sessions) }
    val daily = remember(sessions) { StatsCalculator.calculateDaily(sessions) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            BackTopBar(title = stringResource(Res.string.stats_title), onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                // Summary cards
                Text(stringResource(Res.string.summary), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SummaryCard(
                        label = stringResource(Res.string.sessions),
                        value = "${overall.totalSessions}",
                        modifier = Modifier.weight(1f),
                    )
                    SummaryCard(
                        label = stringResource(Res.string.sessions_completed),
                        value = "${overall.completedSessions}",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val totalMin = overall.totalPracticeTimeMs / 60_000
                    SummaryCard(
                        label = stringResource(Res.string.practice_time),
                        value = if (totalMin < 60) "${totalMin}m" else "${totalMin / 60}h ${totalMin % 60}m",
                        modifier = Modifier.weight(1f),
                    )
                    SummaryCard(
                        label = stringResource(Res.string.best_accuracy),
                        value = "${overall.bestAccuracyCents.toInt()} cents",
                        modifier = Modifier.weight(1f),
                    )
                }

                if (daily.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(28.dp))

                    // Cents accuracy chart
                    ChartSection(
                        title = stringResource(Res.string.accuracy_cents),
                        data = daily.map { it.averageCents },
                        labels = daily.map { it.dateLabel },
                        lineColor = Color(0xFF4CAF50),
                        yAxisLabel = stringResource(Res.string.cents),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // First attempt rate chart
                    ChartSection(
                        title = stringResource(Res.string.first_attempt_pct),
                        data = daily.map { it.firstAttemptRate * 100f },
                        labels = daily.map { it.dateLabel },
                        lineColor = Color(0xFF2196F3),
                        yAxisLabel = stringResource(Res.string.pct),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Reaction time chart
                    ChartSection(
                        title = stringResource(Res.string.reaction_time_ms),
                        data = daily.map { it.averageReactionMs.toFloat() },
                        labels = daily.map { it.dateLabel },
                        lineColor = Color(0xFFFF9800),
                        yAxisLabel = stringResource(Res.string.ms),
                    )
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        stringResource(Res.string.no_stats_yet),
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(16.dp),
    ) {
        Column {
            Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun ChartSection(
    title: String,
    data: List<Float>,
    labels: List<String>,
    lineColor: Color,
    yAxisLabel: String,
) {
    Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
    ) {
        LineChart(
            data = data,
            labels = labels,
            lineColor = lineColor,
            modifier = Modifier.fillMaxSize(),
            yAxisLabel = yAxisLabel,
        )
    }
}
