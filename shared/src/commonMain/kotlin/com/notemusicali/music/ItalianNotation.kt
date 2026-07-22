package com.notemusicali.music

/**
 * Maps note indices (0-11) to Italian notation names.
 * 0=DO(C), 1=DO#, 2=RE(D), ... 11=SI(B)
 */
object ItalianNotation {

    data class NoteName(
        val sharp: String,
        val flat: String?,
    ) {
        val display: String get() = sharp
    }

    private val NOTE_NAMES = arrayOf(
        NoteName("DO", null),        // 0 - C
        NoteName("DO#", "REb"),      // 1 - C#/Db
        NoteName("RE", null),        // 2 - D
        NoteName("RE#", "MIb"),      // 3 - D#/Eb
        NoteName("MI", null),        // 4 - E
        NoteName("FA", null),        // 5 - F
        NoteName("FA#", "SOLb"),     // 6 - F#/Gb
        NoteName("SOL", null),       // 7 - G
        NoteName("SOL#", "LAb"),     // 8 - G#/Ab
        NoteName("LA", null),        // 9 - A
        NoteName("LA#", "SIb"),      // 10 - A#/Bb
        NoteName("SI", null),        // 11 - B
    )

    fun fromNoteIndex(index: Int): NoteName {
        return NOTE_NAMES[index % 12]
    }

    fun allNotes(): List<NoteName> = NOTE_NAMES.toList()

    /**
     * Parse an Italian note name to its index (0-11).
     * Accepts both sharp and flat variants.
     */
    fun toNoteIndex(name: String): Int? {
        val upper = name.uppercase().trim()
        return NOTE_NAMES.indexOfFirst { noteName ->
            noteName.sharp.uppercase() == upper ||
                noteName.flat?.uppercase() == upper
        }.takeIf { it >= 0 }
    }

    /**
     * Map MusicXML pitch step (C,D,E,F,G,A,B) + alter to note index.
     */
    fun fromMusicXmlStep(step: String, alter: Int = 0): Int? {
        val baseIndex = when (step.uppercase()) {
            "C" -> 0
            "D" -> 2
            "E" -> 4
            "F" -> 5
            "G" -> 7
            "A" -> 9
            "B" -> 11
            else -> return null
        }
        return ((baseIndex + alter) + 12) % 12
    }
}
