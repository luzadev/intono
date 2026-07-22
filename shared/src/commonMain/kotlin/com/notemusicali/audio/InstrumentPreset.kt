package com.notemusicali.audio

enum class InstrumentPreset(
    val displayName: String,
    val minFrequency: Float,
    val maxFrequency: Float,
    val yinThreshold: Float,
    val bufferSize: Int,
    val minOctave: Int,
    val maxOctave: Int,
    val description: String,
) {
    VIOLINO(
        displayName = "Violino",
        minFrequency = 180f,
        maxFrequency = 2700f,
        yinThreshold = 0.15f,
        bufferSize = 2048,
        minOctave = 3,
        maxOctave = 7,
        description = "G3-E7 \u2022 Ottimizzato per archi",
    ),
    VIOLA(
        displayName = "Viola",
        minFrequency = 120f,
        maxFrequency = 1800f,
        yinThreshold = 0.15f,
        bufferSize = 2048,
        minOctave = 3,
        maxOctave = 6,
        description = "C3-A6 \u2022 Ottimizzato per archi",
    ),
    VIOLONCELLO(
        displayName = "Violoncello",
        minFrequency = 60f,
        maxFrequency = 1100f,
        yinThreshold = 0.15f,
        bufferSize = 4096,
        minOctave = 2,
        maxOctave = 6,
        description = "C2-C6 \u2022 Buffer ampio per note basse",
    ),
    PIANOFORTE(
        displayName = "Pianoforte",
        minFrequency = 27f,
        maxFrequency = 4200f,
        yinThreshold = 0.10f,
        bufferSize = 4096,
        minOctave = 0,
        maxOctave = 8,
        description = "A0-C8 \u2022 Soglia bassa, segnale pulito",
    ),
    CHITARRA(
        displayName = "Chitarra",
        minFrequency = 78f,
        maxFrequency = 1400f,
        yinThreshold = 0.12f,
        bufferSize = 2048,
        minOctave = 2,
        maxOctave = 6,
        description = "E2-E6 \u2022 Corde pizzicate",
    ),
    VOCE(
        displayName = "Voce",
        minFrequency = 75f,
        maxFrequency = 1200f,
        yinThreshold = 0.15f,
        bufferSize = 2048,
        minOctave = 2,
        maxOctave = 6,
        description = "D2-D6 \u2022 Range vocale esteso",
    ),

    ;

    val octaveRange: IntRange get() = minOctave..maxOctave
}
