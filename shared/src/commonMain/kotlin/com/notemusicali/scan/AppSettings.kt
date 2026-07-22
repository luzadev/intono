package com.notemusicali.scan

import com.russhwolf.settings.Settings

/**
 * Cross-platform settings accessor. API keys go to the platform secure store
 * when available (iOS Keychain, Android EncryptedSharedPreferences); other
 * values use multiplatform-settings with basic obfuscation for sensitive-ish
 * content.
 */
object AppSettings {
    private val store by lazy { ApiKeyStore(Settings(), createSecureSettings()) }

    fun getString(key: String): String? = store.getString(key)

    fun putString(key: String, value: String) = store.putString(key, value)

    /**
     * Migrate any existing plaintext values to obfuscated format.
     * Call once at app startup.
     */
    fun migrateIfNeeded() {
        store.migratePlaintextIfNeeded(
            listOf("api_key", "omr_api_key", "claude_api_key", "openai_api_key", "anthropic_api_key"),
        )
    }
}
