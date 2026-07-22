package com.notemusicali.ui.components

import android.content.Intent
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFolderPickerLauncher(
    onFolderSelected: (uri: String, displayName: String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: Exception) {}
            val docId = DocumentsContract.getTreeDocumentId(it)
            val name = docId
                .substringAfterLast(":")
                .substringAfterLast("/")
                .ifEmpty { "Cartella" }
            onFolderSelected(it.toString(), name)
        }
    }
    return { launcher.launch(null) }
}
