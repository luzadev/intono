package com.notemusicali.ui.exercises

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.notemusicali.exercises.Exercise
import com.notemusicali.exercises.ExerciseRepository
import com.notemusicali.exercises.ImportedExercise
import com.notemusicali.exercises.ImportedExerciseRepository
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.notemusicali.music.NoteSequence
import com.notemusicali.ui.components.BackTopBar
import com.notemusicali.ui.components.GradientCard
import com.notemusicali.ui.components.StaffPreview
import com.notemusicali.ui.theme.CardGradients

private val StarGold = Color(0xFFFFD54F)
private const val MAX_STARS = 4

@Composable
fun ExercisesScreen(
    onBack: () -> Unit,
    onExerciseSelected: (NoteSequence) -> Unit,
) {
    var imported by remember { mutableStateOf(ImportedExerciseRepository.getAll()) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
    Column(modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight()) {
        BackTopBar(
            title = stringResource(Res.string.exercises_title),
            onBack = onBack,
            contentColor = MaterialTheme.colorScheme.primary,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        ) {
            items(ExerciseRepository.all) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onClick = { onExerciseSelected(exercise.sequence) },
                )
            }

            if (imported.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.imported_exercises),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(imported, key = { it.id }) { entry ->
                    ImportedExerciseCard(
                        entry = entry,
                        onClick = { onExerciseSelected(entry.toNoteSequence()) },
                        onDelete = {
                            ImportedExerciseRepository.remove(entry.id)
                            imported = ImportedExerciseRepository.getAll()
                        },
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
) {
    GradientCard(
        gradient = CardGradients.forLevel(exercise.level),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = exercise.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            StarRating(level = exercise.level)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = exercise.description,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(8.dp))

        StaffPreview(
            notes = exercise.sequence.notes,
            beats = exercise.sequence.beats,
            beatType = exercise.sequence.beatType,
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.n_notes, exercise.sequence.size),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun StarRating(level: Int) {
    Row {
        repeat(MAX_STARS) { index ->
            Icon(
                imageVector = if (index < level) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = stringResource(Res.string.level_n, index + 1),
                modifier = Modifier.size(20.dp),
                tint = if (index < level) StarGold else Color.White.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun ImportedExerciseCard(
    entry: ImportedExercise,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    GradientCard(
        gradient = CardGradients.scoreFile,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(Res.string.delete_imported),
                    tint = Color.White.copy(alpha = 0.6f),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        StaffPreview(
            notes = entry.toNoteSequence().notes,
            beats = entry.beats,
            beatType = entry.beatType,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.n_notes, entry.notes.size),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f),
        )
    }
}
