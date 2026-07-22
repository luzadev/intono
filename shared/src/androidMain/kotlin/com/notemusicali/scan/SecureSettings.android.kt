package com.notemusicali.scan

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.notemusicali.scores.ScoreProviderContext
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.Settings

internal actual fun createSecureSettings(): Settings? {
    val context = ScoreProviderContext.appContext ?: return null
    return try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            "intono_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
        SharedPreferencesSettings(prefs)
    } catch (_: Exception) {
        // Keystore unavailable (rare): fall back to the obfuscated store
        null
    }
}
