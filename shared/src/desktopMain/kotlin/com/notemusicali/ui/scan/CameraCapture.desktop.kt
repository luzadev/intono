package com.notemusicali.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.scan.PlatformImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun CameraCapture(
    onImageCaptured: (PlatformImage) -> Unit,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "Su desktop non c'è la fotocamera: scegli un'immagine dello spartito dal disco",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = {
                    scope.launch(Dispatchers.Default) {
                        val image = pickImageFile() ?: return@launch
                        withContext(Dispatchers.Main) { onImageCaptured(image) }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF1A0533),
                ),
            ) {
                Text("Scegli immagine…")
            }
        }
    }
}

private fun pickImageFile(): PlatformImage? {
    val chooser = JFileChooser().apply {
        dialogTitle = "Scegli l'immagine dello spartito"
        fileFilter = FileNameExtensionFilter("Immagini (PNG, JPEG)", "png", "jpg", "jpeg")
        isMultiSelectionEnabled = false
    }
    if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return null
    return try {
        ImageIO.read(chooser.selectedFile)
    } catch (_: Exception) {
        null
    }
}
