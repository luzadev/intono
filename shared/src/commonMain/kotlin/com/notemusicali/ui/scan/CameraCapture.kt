package com.notemusicali.ui.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.notemusicali.scan.PlatformImage

/**
 * Platform-specific camera capture composable.
 * On platforms without camera support, shows a placeholder.
 */
@Composable
expect fun CameraCapture(
    onImageCaptured: (PlatformImage) -> Unit,
    modifier: Modifier,
)
