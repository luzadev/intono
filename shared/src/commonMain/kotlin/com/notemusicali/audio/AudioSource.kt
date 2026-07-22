package com.notemusicali.audio

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic audio source that provides audio buffers as a Flow.
 */
interface AudioSource {
    fun audioFlow(): Flow<FloatArray>
}

/**
 * Creates a platform-specific audio source.
 */
expect fun createAudioSource(sampleRate: Int = 44100, bufferSize: Int = 2048): AudioSource
