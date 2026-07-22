package com.notemusicali.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.notemusicali.scores.extractMusicXmlFromBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.awt.KeyboardFocusManager
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberFilePickerLauncher(
    mimeTypes: List<String>,
    onFileContent: (fileName: String, content: String) -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    return {
        // I dialoghi Swing vivono solo sull'Event Dispatch Thread; mostrarli da
        // un thread di background inchioda l'app su macOS
        scope.launch(Dispatchers.Swing) {
            val chooser = JFileChooser().apply {
                fileFilter = FileNameExtensionFilter("MusicXML files", "xml", "musicxml", "mxl")
                dialogTitle = "Seleziona file MusicXML"
            }
            val parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
            val result = chooser.showOpenDialog(parent)
            if (result == JFileChooser.APPROVE_OPTION) {
                val file = chooser.selectedFile
                // Lettura e unzip fuori dall'EDT
                val content = withContext(Dispatchers.Default) {
                    try {
                        extractMusicXmlFromBytes(file.readBytes())
                    } catch (_: Exception) {
                        null
                    }
                }
                // Callback di nuovo sull'EDT: aggiorna stato Compose e naviga
                if (content != null) {
                    onFileContent(file.name, content)
                }
            }
        }
    }
}
