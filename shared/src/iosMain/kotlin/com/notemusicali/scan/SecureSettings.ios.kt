package com.notemusicali.scan

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

@OptIn(ExperimentalSettingsImplementation::class)
internal actual fun createSecureSettings(): Settings? = try {
    KeychainSettings(service = "com.notemusicali.intono")
} catch (_: Throwable) {
    null
}
