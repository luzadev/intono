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
    onFileContent: (fileName: String, content: String) -> Unit,
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
                        onFileContent(displayNameOf(context, it), content)
                    }
                }
            } catch (_: Exception) {
                // File read failed
            }
        }
    }

    return { launcher.launch(mimeTypes.toTypedArray()) }
}

private fun displayNameOf(context: android.content.Context, uri: Uri): String {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        } ?: (uri.lastPathSegment ?: "Import")
    } catch (_: Exception) {
        uri.lastPathSegment ?: "Import"
    }
}
