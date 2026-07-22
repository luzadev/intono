package com.notemusicali

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.notemusicali.navigation.AppNavigation
import com.notemusicali.scan.AppSettings
import com.notemusicali.ui.theme.NoteMusicaliTheme
import com.notemusicali.util.CrashHandler

fun MainViewController() = ComposeUIViewController {
    CrashHandler.install()
    AppSettings.migrateIfNeeded()
    NoteMusicaliTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AppNavigation()
        }
    }
}
