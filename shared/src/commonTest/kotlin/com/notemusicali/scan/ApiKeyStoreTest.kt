package com.notemusicali.scan

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiKeyStoreTest {

    @Test
    fun `api key is written to the secure store when available`() {
        val plain = MapSettings()
        val secure = MapSettings()
        val store = ApiKeyStore(plain, secure)

        store.putString("anthropic_api_key", "sk-ant-secret")

        assertEquals("sk-ant-secret", secure.getStringOrNull("anthropic_api_key"))
        assertNull(plain.getStringOrNull("anthropic_api_key"), "the plain store must not receive the key")
        assertEquals("sk-ant-secret", store.getString("anthropic_api_key"))
    }

    @Test
    fun `legacy obfuscated key is migrated to the secure store on read`() {
        val plain = MapSettings()
        val legacyStore = ApiKeyStore(plain, secure = null)
        legacyStore.putString("openai_api_key", "sk-legacy")

        val secure = MapSettings()
        val store = ApiKeyStore(plain, secure)

        assertEquals("sk-legacy", store.getString("openai_api_key"))
        assertEquals("sk-legacy", secure.getStringOrNull("openai_api_key"))
        assertNull(plain.getStringOrNull("openai_api_key"), "legacy copy must be removed after migration")
    }

    @Test
    fun `without a secure store keys stay obfuscated in the plain store`() {
        val plain = MapSettings()
        val store = ApiKeyStore(plain, secure = null)

        store.putString("anthropic_api_key", "sk-fallback")

        val raw = plain.getStringOrNull("anthropic_api_key")
        assertTrue(raw != null && raw.startsWith("OBF:") && !raw.contains("sk-fallback"))
        assertEquals("sk-fallback", store.getString("anthropic_api_key"))
    }

    @Test
    fun `non sensitive keys stay in the plain store even when secure exists`() {
        val plain = MapSettings()
        val secure = MapSettings()
        val store = ApiKeyStore(plain, secure)

        store.putString("scores_folder_uri", "/some/path")

        assertNull(secure.getStringOrNull("scores_folder_uri"))
        assertEquals("/some/path", store.getString("scores_folder_uri"))
    }
}
