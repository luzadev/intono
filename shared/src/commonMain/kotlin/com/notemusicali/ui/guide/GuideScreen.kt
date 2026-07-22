package com.notemusicali.ui.guide

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.ui.components.BackTopBar
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun GuideScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            BackTopBar(title = stringResource(Res.string.guide_title), onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                // --- Strumenti ---
                GuideSection(stringResource(Res.string.section_tools))

                GuideItem(
                    stringResource(Res.string.tuner_title),
                    stringResource(Res.string.guide_tuner_desc),
                )

                GuideItem(
                    stringResource(Res.string.metronome_title),
                    stringResource(Res.string.guide_metronome_desc),
                )

                GuideItem(
                    stringResource(Res.string.reference_title),
                    stringResource(Res.string.guide_reference_desc),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Pratica ---
                GuideSection(stringResource(Res.string.section_practice))

                GuideItem(
                    stringResource(Res.string.practice_title),
                    stringResource(Res.string.guide_practice_desc),
                )

                GuideItem(
                    stringResource(Res.string.exercises_title),
                    stringResource(Res.string.guide_exercises_desc),
                )

                GuideItem(
                    stringResource(Res.string.scales_title),
                    stringResource(Res.string.guide_scales_desc),
                )

                GuideItem(
                    stringResource(Res.string.scores_title),
                    stringResource(Res.string.guide_scores_desc),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Sfida ---
                GuideSection(stringResource(Res.string.section_challenge))

                GuideItem(
                    stringResource(Res.string.ear_training_title),
                    stringResource(Res.string.guide_ear_training_desc),
                )

                GuideItem(
                    stringResource(Res.string.challenge_title),
                    stringResource(Res.string.guide_challenge_desc),
                )

                GuideItem(
                    stringResource(Res.string.leaderboard_title),
                    stringResource(Res.string.guide_leaderboard_desc),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Progressi ---
                GuideSection(stringResource(Res.string.section_progress))

                GuideItem(
                    stringResource(Res.string.stats_title),
                    stringResource(Res.string.guide_stats_desc),
                )

                GuideItem(
                    stringResource(Res.string.goals_title),
                    stringResource(Res.string.guide_goals_desc),
                )

                GuideItem(
                    stringResource(Res.string.history_title),
                    stringResource(Res.string.guide_history_desc),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Suggerimenti ---
                GuideSection(stringResource(Res.string.guide_section_tips))

                GuideTip(stringResource(Res.string.guide_tip_quiet))
                GuideTip(stringResource(Res.string.guide_tip_distance))
                GuideTip(stringResource(Res.string.guide_tip_daily))
                GuideTip(stringResource(Res.string.guide_tip_scales))
                GuideTip(stringResource(Res.string.guide_tip_ear))

                Spacer(modifier = Modifier.height(16.dp))

                // --- Supporto ---
                GuideSection(stringResource(Res.string.guide_section_support))

                Text(
                    text = stringResource(Res.string.guide_support_desc),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 20.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val uriHandler = LocalUriHandler.current
                val email = stringResource(Res.string.guide_support_email)

                Button(
                    onClick = {
                        uriHandler.openUri("mailto:$email?subject=InTono%20-%20Feedback")
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.guide_support_button),
                            color = Color.White,
                        )
                    }
                }

                Text(
                    text = email,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Informazioni ---
                GuideSection(stringResource(Res.string.guide_section_info))

                Text(
                    text = stringResource(Res.string.info_version, "1.0"),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.info_developer),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                val website = stringResource(Res.string.info_website)
                Text(
                    text = website,
                    fontSize = 14.sp,
                    color = Color(0xFF90CAF9),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { uriHandler.openUri("https://$website") }
                        .padding(vertical = 4.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                val privacyUrl = stringResource(Res.string.info_privacy_url)
                Button(
                    onClick = { uriHandler.openUri(privacyUrl) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(Res.string.info_privacy_policy),
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun GuideSection(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

@Composable
private fun GuideItem(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f),
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun GuideTip(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Color.White.copy(alpha = 0.6f),
        lineHeight = 20.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
    )
}
