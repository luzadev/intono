package com.notemusicali.ui.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.exercises.ImportedExerciseRepository
import com.notemusicali.music.MusicXmlParser
import com.notemusicali.music.NoteSequence
import com.notemusicali.ui.components.BackTopBar
import com.notemusicali.ui.components.GradientCard
import com.notemusicali.ui.components.rememberFilePickerLauncher
import com.notemusicali.ui.theme.CardGradients
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun PracticeSetupScreen(
    onBack: () -> Unit,
    onManualInput: () -> Unit,
    onMusicXmlLoaded: (NoteSequence) -> Unit,
    onScan: () -> Unit = {},
) {
    val filePickerLauncher = rememberFilePickerLauncher(
        mimeTypes = listOf("text/xml", "application/xml", "*/*"),
        onFileContent = { fileName, content ->
            try {
                val title = fileName
                    .removeSuffix(".musicxml").removeSuffix(".music.xml")
                    .removeSuffix(".xml").removeSuffix(".mxl")
                    .replace('_', ' ')
                    .ifBlank { "MusicXML Import" }
                val sequence = MusicXmlParser.parse(content, title)
                if (sequence.notes.isNotEmpty()) {
                    // Lo spartito importato resta disponibile tra gli esercizi
                    ImportedExerciseRepository.add(title, sequence)
                    onMusicXmlLoaded(sequence)
                }
            } catch (_: Exception) {
                // Parse failed
            }
        },
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
    Column(
        modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight(),
    ) {
        BackTopBar(
            title = stringResource(Res.string.practice_title),
            onBack = onBack,
            contentColor = MaterialTheme.colorScheme.primary,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.practice_choose),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(32.dp))

            SetupCard(
                icon = Icons.Outlined.Edit,
                title = stringResource(Res.string.manual_input_title),
                description = stringResource(Res.string.manual_input_desc),
                onClick = onManualInput,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SetupCard(
                icon = Icons.Outlined.FileOpen,
                title = stringResource(Res.string.import_musicxml_title),
                description = stringResource(Res.string.import_musicxml_desc),
                onClick = filePickerLauncher,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SetupCard(
                icon = Icons.Outlined.CameraAlt,
                title = stringResource(Res.string.scan_title),
                description = stringResource(Res.string.scan_desc),
                onClick = onScan,
            )
        }
    }
    }
}

@Composable
private fun SetupCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    GradientCard(
        gradient = CardGradients.setup,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = Color.White.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}
