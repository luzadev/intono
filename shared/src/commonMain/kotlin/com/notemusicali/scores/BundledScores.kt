package com.notemusicali.scores

import notemusicali.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Curated set of scores shipped inside the app as compose resources, so every
 * platform has content available even before the user picks a folder.
 */
object BundledScores {

    private const val PREFIX = "bundled:"

    // Resource file name (under files/scores/) to display name
    private val manifest = listOf(
        "happy_birthday.mxl" to "Happy Birthday",
        "ode_to_joy.mxl" to "Inno alla Gioia — Beethoven",
        "fur_elise.mxl" to "Per Elisa — Beethoven",
        "minuetto_bach.mxl" to "Minuetto in Sol — Bach",
        "preludio_bach.mxl" to "Preludio in Do — Bach",
        "canone_pachelbel.mxl" to "Canone in Re — Pachelbel",
        "gymnopedie_satie.mxl" to "Gymnopédie n.1 — Satie",
        "notturno_chopin.mxl" to "Notturno op.9 n.2 — Chopin",
        "lago_dei_cigni.mxl" to "Il Lago dei Cigni — Čajkovskij",
        "greensleeves.mxl" to "Greensleeves",
        "carol_of_the_bells.mxl" to "Carol of the Bells",
        "bella_ciao.mxl" to "Bella Ciao",
    )

    val entries: List<ScoreEntry> = manifest.map { (file, displayName) ->
        ScoreEntry(name = displayName, path = PREFIX + file, isDirectory = false)
    }

    fun isBundled(path: String): Boolean = path.startsWith(PREFIX)

    @OptIn(ExperimentalResourceApi::class)
    suspend fun read(path: String): ByteArray? {
        if (!isBundled(path)) return null
        val file = path.removePrefix(PREFIX)
        if (manifest.none { it.first == file }) return null
        return try {
            Res.readBytes("files/scores/$file")
        } catch (_: Throwable) {
            null
        }
    }
}
