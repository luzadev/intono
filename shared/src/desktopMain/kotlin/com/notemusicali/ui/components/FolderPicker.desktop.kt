package com.notemusicali.ui.components

import androidx.compose.runtime.Composable
import javax.swing.JFileChooser

@Composable
actual fun rememberFolderPickerLauncher(
    onFolderSelected: (uri: String, displayName: String) -> Unit,
): () -> Unit {
    return {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Scegli cartella spartiti"
        }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = chooser.selectedFile
            onFolderSelected(file.absolutePath, file.name)
        }
    }
}
