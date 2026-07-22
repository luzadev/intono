package com.notemusicali

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.notemusicali.navigation.AppNavigation
import com.notemusicali.ui.theme.NoteMusicaliTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "InTono",
        state = rememberWindowState(width = 1024.dp, height = 768.dp),
    ) {
        NoteMusicaliTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                AppNavigation()
            }
        }
    }
}
