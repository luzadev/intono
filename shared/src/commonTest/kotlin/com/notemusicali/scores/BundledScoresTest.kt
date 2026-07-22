package com.notemusicali.scores

import com.notemusicali.music.MusicXmlParser
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BundledScoresTest {

    @Test
    fun `manifest lists bundled scores as non-directory entries with bundled prefix`() {
        val entries = BundledScores.entries
        assertTrue(entries.size >= 10, "expected a curated set of scores, got ${entries.size}")
        entries.forEach { entry ->
            assertTrue(BundledScores.isBundled(entry.path), "path ${entry.path} must carry the bundled prefix")
            assertTrue(!entry.isDirectory, "bundled scores are plain files")
            assertTrue(entry.name.isNotBlank() && !entry.name.contains('_'), "display name should be human readable, got ${entry.name}")
        }
    }

    @Test
    fun `non-bundled paths are not recognized as bundled`() {
        assertTrue(!BundledScores.isBundled("/storage/scores/foo.mxl"))
        assertTrue(!BundledScores.isBundled(""))
    }

    @Test
    fun `bundled score can be read and parsed into notes`() = runTest {
        val entry = BundledScores.entries.first()
        val bytes = BundledScores.read(entry.path)
        assertNotNull(bytes, "bundled score ${entry.path} must be readable")
        val xml = extractMusicXmlFromBytes(bytes)
        assertNotNull(xml, "bundled score must contain MusicXML")
        val sequence = MusicXmlParser.parse(xml, entry.name)
        assertTrue(sequence.notes.isNotEmpty(), "parsed sequence must contain notes")
    }

    @Test
    fun `reading an unknown bundled path returns null`() = runTest {
        assertTrue(BundledScores.read("bundled:does_not_exist.mxl") == null)
    }
}
