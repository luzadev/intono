package com.notemusicali.music

/**
 * Ordered list of notes for practice mode.
 */
data class NoteSequence(
    val name: String,
    val notes: List<MusicalNote>,
    val beats: Int = 4,
    val beatType: Int = 4,
) {
    val size: Int get() = notes.size

    fun noteAt(index: Int): MusicalNote? = notes.getOrNull(index)

    companion object {
        fun empty(name: String = "Nuova Sequenza"): NoteSequence {
            return NoteSequence(name = name, notes = emptyList())
        }
    }
}
