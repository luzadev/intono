package com.notemusicali.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.notemusicali.scores.extractMusicXmlFromBytes

@Composable
actual fun rememberFilePickerLauncher(
    mimeTypes: List<String>,
    onFileContent: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bytes = stream.readBytes()
                    val content = extractMusicXmlFromBytes(bytes)
                    if (content != null) {
                        onFileContent(content)
                    }
                }
            } catch (_: Exception) {
                // File read failed
            }
        }
    }

    return { launcher.launch(mimeTypes.toTypedArray()) }
}
