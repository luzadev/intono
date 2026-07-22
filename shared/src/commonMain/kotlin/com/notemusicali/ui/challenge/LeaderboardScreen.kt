package com.notemusicali.ui.challenge

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
import com.notemusicali.challenge.ChallengeRepository
import com.notemusicali.ui.components.BackTopBar
import com.notemusicali.util.formatEpochDate
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val results = remember { ChallengeRepository.load() }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            BackTopBar(title = stringResource(Res.string.leaderboard_title), onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                if (results.isEmpty()) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        stringResource(Res.string.no_results),
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    results.forEachIndexed { index, result ->
                        val rankColor = when (index) {
                            0 -> Color(0xFFFFD54F)
                            1 -> Color(0xFFB0BEC5)
                            2 -> Color(0xFFCD7F32)
                            else -> Color.White.copy(alpha = 0.6f)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .padding(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = rankColor,
                                    modifier = Modifier.widthIn(min = 48.dp),
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = result.sequenceName,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = "${result.notesPlayed} note  •  ${result.maxCombo}x combo  •  ${result.timeLimitSec}s",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.4f),
                                    )
                                    Text(
                                        text = formatEpochDate(result.dateMillis),
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.3f),
                                    )
                                }

                                Text(
                                    text = "${result.totalScore}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = rankColor,
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
