package com.notemusicali.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.notemusicali.scores.extractMusicXmlFromBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberFilePickerLauncher(
    mimeTypes: List<String>,
    onFileContent: (String) -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    return {
        scope.launch(Dispatchers.Default) {
            val chooser = JFileChooser().apply {
                fileFilter = FileNameExtensionFilter("MusicXML files", "xml", "musicxml", "mxl")
                dialogTitle = "Seleziona file MusicXML"
            }
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    val bytes = chooser.selectedFile.readBytes()
                    val content = extractMusicXmlFromBytes(bytes)
                    if (content != null) {
                        onFileContent(content)
                    }
                } catch (_: Exception) {
                    // File read failed
                }
            }
        }
    }
}
