package com.notemusicali.ui.scores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.music.MusicXmlParser
import com.notemusicali.music.NoteSequence
import com.notemusicali.scan.AppSettings
import com.notemusicali.scores.BundledScores
import com.notemusicali.scores.ScoreEntry
import com.notemusicali.scores.ScoreProvider
import com.notemusicali.scores.extractMusicXmlFromBytes
import com.notemusicali.ui.components.BackTopBar
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.notemusicali.ui.components.GradientCard
import com.notemusicali.ui.components.rememberFolderPickerLauncher
import com.notemusicali.ui.theme.CardGradients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScoresScreen(
    onBack: () -> Unit,
    onScoreSelected: (NoteSequence) -> Unit,
) {
    var externalUri by remember {
        mutableStateOf(AppSettings.getString("scores_folder_uri")?.ifEmpty { null })
    }
    var externalName by remember {
        mutableStateOf(AppSettings.getString("scores_folder_name") ?: "")
    }
    var pathStack by remember { mutableStateOf(listOf<String>()) }
    val currentPath = pathStack.lastOrNull() ?: ""

    val entries = remember(externalUri, currentPath) {
        if (externalUri != null) {
            ScoreProvider.listFromUri(externalUri!!, currentPath)
        } else {
            // App source: curated bundled scores first, then any file the user
            // dropped in the app's scores directory
            val bundled = if (currentPath.isEmpty()) BundledScores.entries else emptyList()
            bundled + ScoreProvider.listScores(currentPath)
        }
    }

    var loadingPath by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val folderPicker = rememberFolderPickerLauncher { uri, name ->
        externalUri = uri
        externalName = name
        pathStack = emptyList()
        AppSettings.putString("scores_folder_uri", uri)
        AppSettings.putString("scores_folder_name", name)
    }

    val scoresTitle = stringResource(Res.string.scores_title)
    val title = if (pathStack.isEmpty()) {
        if (externalUri != null) externalName.ifEmpty { scoresTitle } else scoresTitle
    } else {
        val last = pathStack.last()
        last.substringAfterLast("/").substringAfterLast(":").ifEmpty { last }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight()) {
            BackTopBar(
                title = title,
                onBack = {
                    if (pathStack.isNotEmpty()) {
                        pathStack = pathStack.dropLast(1)
                    } else {
                        onBack()
                    }
                },
                contentColor = MaterialTheme.colorScheme.primary,
            )

            // Source selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (externalUri != null) stringResource(Res.string.folder_named, externalName) else stringResource(Res.string.folder_app),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TextButton(onClick = { folderPicker() }) {
                    Icon(
                        Icons.Outlined.Folder,
                        contentDescription = stringResource(Res.string.change_folder),
                        modifier = Modifier.size(16.dp),
                        tint = Color.White.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.change), fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                }
                if (externalUri != null) {
                    TextButton(onClick = {
                        externalUri = null
                        externalName = ""
                        pathStack = emptyList()
                        AppSettings.putString("scores_folder_uri", "")
                        AppSettings.putString("scores_folder_name", "")
                    }) {
                        Text("Reset", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.no_scores_found),
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    items(entries) { entry ->
                        ScoreEntryCard(
                            entry = entry,
                            isLoading = loadingPath == entry.path,
                            onClick = {
                                if (entry.isDirectory) {
                                    pathStack = pathStack + entry.path
                                } else if (loadingPath == null) {
                                    loadingPath = entry.path
                                    scope.launch {
                                        val sequence = withContext(Dispatchers.Default) {
                                            val bytes = when {
                                                BundledScores.isBundled(entry.path) -> BundledScores.read(entry.path)
                                                externalUri != null -> ScoreProvider.readFromUri(externalUri!!, entry.path)
                                                else -> ScoreProvider.readScoreBytes(entry.path)
                                            }
                                            bytes ?: return@withContext null
                                            val xml = extractMusicXmlFromBytes(bytes)
                                                ?: return@withContext null
                                            val seq = MusicXmlParser.parse(xml, entry.name)
                                            if (seq.notes.isNotEmpty()) seq else null
                                        }
                                        loadingPath = null
                                        if (sequence != null) {
                                            onScoreSelected(sequence)
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreEntryCard(
    entry: ScoreEntry,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    GradientCard(
        gradient = if (entry.isDirectory) CardGradients.scores else CardGradients.scoreFile,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = if (entry.isDirectory) Icons.Outlined.Folder else Icons.Outlined.MusicNote,
                    contentDescription = if (entry.isDirectory) stringResource(Res.string.folder) else stringResource(Res.string.score_file),
                    modifier = Modifier.size(28.dp),
                    tint = Color.White.copy(alpha = 0.7f),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = entry.name,
                fontSize = 16.sp,
                fontWeight = if (entry.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
