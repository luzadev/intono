package com.notemusicali.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notemusicali.music.MusicalNote
import com.notemusicali.scan.AppSettings
import com.notemusicali.scan.ClaudeOmrProcessor
import com.notemusicali.scan.OpenAiOmrProcessor
import com.notemusicali.scan.PlatformImage
import com.notemusicali.scan.toBase64Jpeg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {

    sealed class ScanState {
        data object Camera : ScanState()
        data object Processing : ScanState()
        data class Review(val notes: List<MusicalNote>) : ScanState()
        data class Error(val message: String) : ScanState()
    }

    private val _state = MutableStateFlow<ScanState>(ScanState.Camera)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private val _showApiKeyDialog = MutableStateFlow(!hasApiKey())
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    private val _selectedProvider = MutableStateFlow(loadProvider())
    val selectedProvider: StateFlow<AiProvider> = _selectedProvider.asStateFlow()

    fun hasApiKey(): Boolean {
        val provider = loadProvider()
        return getApiKey(provider) != null
    }

    fun getApiKey(provider: AiProvider = _selectedProvider.value): String? {
        val key = when (provider) {
            AiProvider.CLAUDE -> "anthropic_api_key"
            AiProvider.OPENAI -> "openai_api_key"
        }
        return AppSettings.getString(key)?.takeIf { it.isNotBlank() }
    }

    fun saveApiKey(key: String, provider: AiProvider = _selectedProvider.value) {
        val prefKey = when (provider) {
            AiProvider.CLAUDE -> "anthropic_api_key"
            AiProvider.OPENAI -> "openai_api_key"
        }
        AppSettings.putString(prefKey, key.trim())
        _showApiKeyDialog.value = false
    }

    fun selectProvider(provider: AiProvider) {
        _selectedProvider.value = provider
        AppSettings.putString("selected_provider", provider.name)
        if (getApiKey(provider) == null) {
            _showApiKeyDialog.value = true
        }
    }

    private fun loadProvider(): AiProvider {
        val name = AppSettings.getString("selected_provider")
        return try {
            name?.let { AiProvider.valueOf(it) } ?: AiProvider.CLAUDE
        } catch (_: Exception) {
            AiProvider.CLAUDE
        }
    }

    fun requestApiKey() {
        _showApiKeyDialog.value = true
    }

    fun dismissApiKeyDialog() {
        _showApiKeyDialog.value = false
    }

    fun captureAndAnalyze(image: PlatformImage) {
        val provider = _selectedProvider.value
        val apiKey = getApiKey(provider)
        if (apiKey == null) {
            _showApiKeyDialog.value = true
            return
        }

        _state.value = ScanState.Processing

        viewModelScope.launch {
            val base64 = image.toBase64Jpeg()
            val result = when (provider) {
                AiProvider.CLAUDE -> ClaudeOmrProcessor.analyze(base64, apiKey)
                AiProvider.OPENAI -> OpenAiOmrProcessor.analyze(base64, apiKey)
            }

            result.fold(
                onSuccess = { notes ->
                    _state.value = ScanState.Review(notes)
                },
                onFailure = { error ->
                    _state.value = ScanState.Error(
                        error.message ?: "Errore sconosciuto durante l'analisi"
                    )
                },
            )
        }
    }

    fun removeNote(index: Int) {
        val current = _state.value
        if (current is ScanState.Review) {
            val updated = current.notes.toMutableList().apply { removeAt(index) }
            _state.value = ScanState.Review(updated)
        }
    }

    fun retryCamera() {
        _state.value = ScanState.Camera
    }
}
