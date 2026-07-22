package com.notemusicali.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.notemusicali.audio.InstrumentPreset
import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteSequence
import com.notemusicali.ui.components.BackTopBar
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onSequenceCaptured: (NoteSequence, InstrumentPreset) -> Unit,
    viewModel: ScanViewModel,
) {
    val state by viewModel.state.collectAsState()
    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()

    if (showApiKeyDialog) {
        ApiKeyDialog(
            provider = selectedProvider,
            onProviderChange = { viewModel.selectProvider(it) },
            onConfirm = { key -> viewModel.saveApiKey(key) },
            onDismiss = {
                viewModel.dismissApiKeyDialog()
                if (!viewModel.hasApiKey()) onBack()
            },
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
    Column(modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight()) {
        BackTopBar(
            title = stringResource(Res.string.scan_title),
            onBack = onBack,
            contentColor = when (state) {
                is ScanViewModel.ScanState.Camera -> Color.White
                else -> MaterialTheme.colorScheme.primary
            },
            containerColor = Color.Transparent,
        )

        when (val currentState = state) {
            is ScanViewModel.ScanState.Camera -> CameraView(
                onCapture = { image -> viewModel.captureAndAnalyze(image) },
                onProviderClick = { viewModel.requestApiKey() },
                providerName = selectedProvider.displayName,
            )
            is ScanViewModel.ScanState.Processing -> ProcessingView()
            is ScanViewModel.ScanState.Review -> ReviewView(
                notes = currentState.notes,
                onRemoveNote = { viewModel.removeNote(it) },
                onRetry = { viewModel.retryCamera() },
                onStartPractice = { instrument ->
                    onSequenceCaptured(
                        NoteSequence(
                            name = "Spartito Scansionato",
                            notes = currentState.notes,
                        ),
                        instrument,
                    )
                },
            )
            is ScanViewModel.ScanState.Error -> ErrorView(
                message = currentState.message,
                onRetry = { viewModel.retryCamera() },
                onChangeKey = { viewModel.requestApiKey() },
            )
        }
    }
    }
}

@Composable
private fun CameraView(
    onCapture: (com.notemusicali.scan.PlatformImage) -> Unit,
    onProviderClick: () -> Unit,
    providerName: String,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraCapture(
            onImageCaptured = onCapture,
            modifier = Modifier.fillMaxSize(),
        )

        // Provider badge
        Text(
            text = providerName,
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onProviderClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ProcessingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.analyzing),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(Res.string.analyzing_desc),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun ReviewView(
    notes: List<MusicalNote>,
    onRemoveNote: (Int) -> Unit,
    onRetry: () -> Unit,
    onStartPractice: (InstrumentPreset) -> Unit,
) {
    var selectedInstrument by remember { mutableStateOf(InstrumentPreset.VIOLINO) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.notes_detected, notes.size),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (notes.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp),
                    )
                    .padding(12.dp),
            ) {
                itemsIndexed(notes) { index, note ->
                    val chipShape = RoundedCornerShape(8.dp)
                    val chipGradient = Brush.linearGradient(
                        listOf(Color(0xFF1565C0), Color(0xFF7B1FA2)),
                    )
                    Text(
                        text = note.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier
                            .clip(chipShape)
                            .background(chipGradient)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), chipShape)
                            .clickable { onRemoveNote(index) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }

            Text(
                text = stringResource(Res.string.tap_to_remove),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.instrument),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InstrumentPreset.entries.forEach { preset ->
                FilterChip(
                    selected = selectedInstrument == preset,
                    onClick = { selectedInstrument = preset },
                    label = { Text(preset.displayName, fontSize = 13.sp) },
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onStartPractice(selectedInstrument) },
            enabled = notes.size >= 2,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.start_practice_n, notes.size), fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.take_again))
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onChangeKey: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = stringResource(Res.string.error),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.retry))
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = onChangeKey, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.change_api_key))
            }
        }
    }
}

@Composable
private fun ApiKeyDialog(
    provider: AiProvider,
    onProviderChange: (AiProvider) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var apiKey by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.api_key),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AiProvider.entries.forEach { p ->
                        FilterChip(
                            selected = provider == p,
                            onClick = { onProviderChange(p) },
                            label = { Text(p.displayName, fontSize = 12.sp) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (provider) {
                        AiProvider.CLAUDE -> stringResource(Res.string.enter_anthropic_key)
                        AiProvider.OPENAI -> stringResource(Res.string.enter_openai_key)
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(stringResource(Res.string.api_key)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (provider) {
                        AiProvider.CLAUDE -> stringResource(Res.string.get_anthropic_key)
                        AiProvider.OPENAI -> stringResource(Res.string.get_openai_key)
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }

                    Button(
                        onClick = { onConfirm(apiKey) },
                        enabled = apiKey.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(Res.string.save))
                    }
                }
            }
        }
    }
}
