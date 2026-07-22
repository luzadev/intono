package com.notemusicali.ui.challenge

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
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.challenge.ChallengeTimeLimit
import com.notemusicali.exercises.ExerciseRepository
import com.notemusicali.music.NoteSequence
import com.notemusicali.music.ScaleDirection
import com.notemusicali.music.ScaleGenerator
import com.notemusicali.music.ScaleType
import com.notemusicali.ui.components.BackTopBar
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

private enum class SequenceSource {
    EXERCISES,
    SCALES,
    RANDOM,
}

@Composable
fun ChallengeSetupScreen(
    onBack: () -> Unit,
    onStart: (NoteSequence, ChallengeTimeLimit) -> Unit,
) {
    var selectedSource by remember { mutableStateOf(SequenceSource.EXERCISES) }
    var selectedTimeLimit by remember { mutableStateOf(ChallengeTimeLimit.MEDIUM) }

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
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(Res.string.section_challenge),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(Res.string.challenge_subtitle),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Sequence source
                Text(stringResource(Res.string.sequence), fontSize = 16.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SequenceSource.entries.forEach { source ->
                        val sourceLabel = when (source) {
                            SequenceSource.EXERCISES -> stringResource(Res.string.source_exercises)
                            SequenceSource.SCALES -> stringResource(Res.string.source_scales)
                            SequenceSource.RANDOM -> stringResource(Res.string.source_random)
                        }
                        FilterChip(
                            selected = selectedSource == source,
                            onClick = { selectedSource = source },
                            label = { Text(sourceLabel) },
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

                // Time limit
                Text(stringResource(Res.string.time_limit), fontSize = 16.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChallengeTimeLimit.entries.forEach { limit ->
                        FilterChip(
                            selected = selectedTimeLimit == limit,
                            onClick = { selectedTimeLimit = limit },
                            label = { Text(limit.displayName) },
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

                val challengeRandomName = stringResource(Res.string.challenge_random)
                Button(
                    onClick = {
                        val sequence = generateSequence(selectedSource, challengeRandomName)
                        onStart(sequence, selectedTimeLimit)
                    },
                    modifier = Modifier.fillMaxWidth(0.6f),
                ) {
                    Text(stringResource(Res.string.start_challenge), fontSize = 18.sp)
                }
            }
        }
    }
}

private fun generateSequence(source: SequenceSource, challengeRandomName: String): NoteSequence {
    return when (source) {
        SequenceSource.EXERCISES -> {
            val exercises = ExerciseRepository.getAllExercises()
            exercises.randomOrNull() ?: generateRandomSequence(challengeRandomName)
        }
        SequenceSource.SCALES -> {
            val rootNote = Random.nextInt(12)
            ScaleGenerator.generateScale(rootNote, 4, ScaleType.MAJOR, ScaleDirection.BOTH)
        }
        SequenceSource.RANDOM -> generateRandomSequence(challengeRandomName)
    }
}

private fun generateRandomSequence(challengeRandomName: String): NoteSequence {
    val notes = (1..12).map {
        com.notemusicali.music.MusicalNote.fromMidi(Random.nextInt(60, 73))
    }
    return NoteSequence(name = challengeRandomName, notes = notes)
}
