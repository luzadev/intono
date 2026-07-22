package com.notemusicali.ui.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific file picker launcher.
 * Returns a lambda that, when invoked, opens the file picker.
 * [onFileContent] is called with the display name of the file and its content.
 */
@Composable
expect fun rememberFilePickerLauncher(
    mimeTypes: List<String>,
    onFileContent: (fileName: String, content: String) -> Unit,
): () -> Unit
