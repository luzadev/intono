package com.notemusicali.scan

import com.russhwolf.settings.Settings

/**
 * Platform-backed secure key-value store (iOS Keychain, Android
 * EncryptedSharedPreferences). Null when the platform has none — values then
 * stay in the regular store with obfuscation.
 */
internal expect fun createSecureSettings(): Settings?

/**
 * Routes sensitive keys to the secure store when one exists, transparently
 * migrating legacy values out of the plain store. Everything else stays in
 * the plain store (obfuscated, to avoid trivial plaintext inspection).
 */
internal class ApiKeyStore(
    private val plain: Settings,
    private val secure: Settings?,
) {
    private val secureKeys = setOf("anthropic_api_key", "openai_api_key")
    private val obfuscationKey = 0x4D // 'M' for music
    private val hexChars = "0123456789abcdef"

    fun getString(key: String): String? {
        if (key in secureKeys && secure != null) {
            secure.getStringOrNull(key)?.let { return it }
            // One-time migration of a value stored before the secure store existed
            val legacy = plain.getStringOrNull(key)?.let(::deobfuscate) ?: return null
            secure.putString(key, legacy)
            plain.remove(key)
            return legacy
        }
        return plain.getStringOrNull(key)?.let(::deobfuscate)
    }

    fun putString(key: String, value: String) {
        if (key in secureKeys && secure != null) {
            secure.putString(key, value)
            plain.remove(key)
        } else {
            plain.putString(key, obfuscate(value))
        }
    }

    fun migratePlaintextIfNeeded(keys: List<String>) {
        for (key in keys) {
            val raw = plain.getStringOrNull(key) ?: continue
            if (!raw.startsWith("OBF:")) {
                plain.putString(key, obfuscate(raw))
            }
        }
    }

    private fun obfuscate(value: String): String {
        val bytes = value.encodeToByteArray()
        val sb = StringBuilder("OBF:")
        for (b in bytes) {
            val xored = (b.toInt() xor obfuscationKey) and 0xFF
            sb.append(hexChars[xored shr 4])
            sb.append(hexChars[xored and 0x0F])
        }
        return sb.toString()
    }

    private fun deobfuscate(stored: String): String {
        if (!stored.startsWith("OBF:")) return stored // legacy plaintext
        val hex = stored.removePrefix("OBF:")
        val bytes = ByteArray(hex.length / 2) { i ->
            val hi = hex[i * 2].digitToInt(16)
            val lo = hex[i * 2 + 1].digitToInt(16)
            ((hi shl 4 or lo) xor obfuscationKey).toByte()
        }
        return bytes.decodeToString()
    }
}
