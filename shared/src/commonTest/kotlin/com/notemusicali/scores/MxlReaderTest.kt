package com.notemusicali.scores

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalEncodingApi::class)
class MxlReaderTest {

    // Both archives contain META-INF/container.xml plus score.xml (a minimal
    // score-partwise document with one C4 quarter note).
    private val storedMxl = Base64.decode(
        "UEsDBBQAAAAAAAAAIVxZbzWxZAAAAGQAAAAWAAAATUVUQS1JTkYvY29udGFpbmVyLnhtbDw/eG1sIHZlcnNpb249IjEuMCI/Pjxjb250YWluZXI+PHJvb3RmaWxlcz48cm9vdGZpbGUgZnVsbC1wYXRoPSJzY29yZS54bWwiLz48L3Jvb3RmaWxlcz48L2NvbnRhaW5lcj5QSwMEFAAAAAAAAAAhXK2hgKfKAAAAygAAAAkAAABzY29yZS54bWw8P3htbCB2ZXJzaW9uPSIxLjAiIGVuY29kaW5nPSJVVEYtOCI/PjxzY29yZS1wYXJ0d2lzZT48cGFydCBpZD0iUDEiPjxtZWFzdXJlIG51bWJlcj0iMSI+PG5vdGU+PHBpdGNoPjxzdGVwPkM8L3N0ZXA+PG9jdGF2ZT40PC9vY3RhdmU+PC9waXRjaD48dHlwZT5xdWFydGVyPC90eXBlPjwvbm90ZT48L21lYXN1cmU+PC9wYXJ0Pjwvc2NvcmUtcGFydHdpc2U+UEsBAhQDFAAAAAAAAAAhXFlvNbFkAAAAZAAAABYAAAAAAAAAAAAAAIABAAAAAE1FVEEtSU5GL2NvbnRhaW5lci54bWxQSwECFAMUAAAAAAAAACFcraGAp8oAAADKAAAACQAAAAAAAAAAAAAAgAGYAAAAc2NvcmUueG1sUEsFBgAAAAACAAIAewAAAIkBAAAAAA==",
    )

    private val deflatedMxl = Base64.decode(
        "UEsDBBQAAAAIAAAAIVxZbzWxTAAAAGQAAAAWAAAATUVUQS1JTkYvY29udGFpbmVyLnhtbLOxr8jNUShLLSrOzM+zVTLUM1Cyt7NJzs8rSczMSy2ysynKzy9Jy8xJLUYwFdJKc3J0CxJLMmyVipPzi1L1gGYo6dvZ6CMp1keYAQBQSwMEFAAAAAgAAAAhXK2hgKeQAAAAygAAAAkAAABzY29yZS54bWxdjjsOAjEMRK8SpV/CShQUjrdAoqaAA4SsBZHIh3wWuD3Jkopqxvb42TC97YMtFJPxTvJxs+WMnPazcTfJL+fjsOcTQtI+0hBUzC+TCKE5ZmbJTyNHsKRSicRcsVeKlVJ7zueWM1nf63qmgAcQq4LXWS2EOxDdgei5/AmEz1LhFEGsFYgfSfQjLVznVf5++gJQSwECFAMUAAAACAAAACFcWW81sUwAAABkAAAAFgAAAAAAAAAAAAAAgAEAAAAATUVUQS1JTkYvY29udGFpbmVyLnhtbFBLAQIUAxQAAAAIAAAAIVytoYCnkAAAAMoAAAAJAAAAAAAAAAAAAACAAYAAAABzY29yZS54bWxQSwUGAAAAAAIAAgB7AAAANwEAAAAA",
    )

    @Test
    fun `extracts score xml from stored mxl archive skipping META-INF`() {
        val xml = extractMusicXmlFromBytes(storedMxl)
        assertTrue(xml != null && xml.contains("<score-partwise>"), "expected score XML, got: $xml")
        assertTrue(!xml!!.contains("<container>"), "META-INF/container.xml must be skipped")
    }

    @Test
    fun `extracts score xml from deflate-compressed mxl archive`() {
        val xml = extractMusicXmlFromBytes(deflatedMxl)
        assertTrue(xml != null && xml.contains("<octave>4</octave>"), "expected score XML, got: $xml")
    }

    @Test
    fun `passes plain xml through unchanged`() {
        val plain = "<?xml version=\"1.0\"?><score-partwise/>"
        assertEquals(plain, extractMusicXmlFromBytes(plain.encodeToByteArray()))
    }

    @Test
    fun `returns null for corrupt zip data`() {
        val corrupt = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 1, 2, 3, 4, 5)
        assertNull(extractMusicXmlFromBytes(corrupt))
    }
}
