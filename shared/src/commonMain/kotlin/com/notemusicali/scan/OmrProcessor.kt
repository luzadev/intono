package com.notemusicali.scan

import com.notemusicali.music.MusicalNote

/**
 * Interface for Optical Music Recognition processors.
 */
interface OmrProcessor {
    suspend fun analyze(imageBase64: String, apiKey: String): Result<List<MusicalNote>>
}
