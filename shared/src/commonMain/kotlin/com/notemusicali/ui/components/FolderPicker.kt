package com.notemusicali.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFolderPickerLauncher(
    onFolderSelected: (uri: String, displayName: String) -> Unit,
): () -> Unit
