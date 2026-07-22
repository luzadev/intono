package com.notemusicali.exercises

import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteDuration
import com.notemusicali.music.NoteSequence
import com.notemusicali.util.currentTimeMillis
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class StoredNote(
    val midi: Int,
    val duration: NoteDuration = NoteDuration.QUARTER,
    val flat: Boolean = false,
)

@Serializable
data class ImportedExercise(
    val id: String,
    val title: String,
    val notes: List<StoredNote>,
    val beats: Int = 4,
    val beatType: Int = 4,
) {
    fun toNoteSequence(): NoteSequence = NoteSequence(
        name = title,
        notes = notes.map { MusicalNote.fromMidi(it.midi, it.duration, preferFlat = it.flat) },
        beats = beats,
        beatType = beatType,
    )
}

/** Persistenza degli spartiti importati dall'utente (JSON su multiplatform-settings). */
internal class ImportedExerciseStore(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true }

    fun getAll(): List<ImportedExercise> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<ImportedExercise>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(title: String, sequence: NoteSequence, nowMs: Long = currentTimeMillis()): ImportedExercise {
        val exercise = ImportedExercise(
            id = nowMs.toString(),
            title = title,
            notes = sequence.notes.map { StoredNote(it.midiNumber, it.duration, it.preferFlat) },
            beats = sequence.beats,
            beatType = sequence.beatType,
        )
        val updated = (listOf(exercise) + getAll()).take(MAX_IMPORTED)
        settings.putString(KEY, json.encodeToString(updated))
        return exercise
    }

    fun remove(id: String) {
        val remaining = getAll().filterNot { it.id == id }
        settings.putString(KEY, json.encodeToString(remaining))
    }

    private companion object {
        const val KEY = "imported_exercises"
        const val MAX_IMPORTED = 50
    }
}

/** Facade con lo storage reale dell'app. */
object ImportedExerciseRepository {
    private val store by lazy { ImportedExerciseStore(Settings()) }

    fun getAll(): List<ImportedExercise> = store.getAll()
    fun add(title: String, sequence: NoteSequence): ImportedExercise = store.add(title, sequence)
    fun remove(id: String) = store.remove(id)
}
