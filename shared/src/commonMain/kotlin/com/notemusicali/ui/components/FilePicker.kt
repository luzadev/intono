package com.notemusicali.ui.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific file picker launcher.
 * Returns a lambda that, when invoked, opens the file picker.
 * [onFileContent] is called with the file content as a String.
 */
@Composable
expect fun rememberFilePickerLauncher(
    mimeTypes: List<String>,
    onFileContent: (String) -> Unit,
): () -> Unit
