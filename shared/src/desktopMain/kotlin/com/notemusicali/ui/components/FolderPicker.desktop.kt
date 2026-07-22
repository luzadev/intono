package com.notemusicali.ui.components

import androidx.compose.runtime.Composable
import java.awt.KeyboardFocusManager
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
        // Parent = finestra attiva, così il dialogo non finisce dietro l'app
        val parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            val file = chooser.selectedFile
            onFolderSelected(file.absolutePath, file.name)
        }
    }
}
