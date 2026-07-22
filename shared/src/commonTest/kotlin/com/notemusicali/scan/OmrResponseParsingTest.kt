package com.notemusicali.scan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OmrResponseParsingTest {

    @Test
    fun `claude response with note list is parsed into notes`() {
        val body = """{"content":[{"type":"text","text":"C4\nD#5\nBb3"}]}"""
        val result = ClaudeOmrProcessor.parseResponse(body)
        assertTrue(result.isSuccess, "expected success, got $result")
        val notes = result.getOrThrow()
        assertEquals(3, notes.size)
        assertEquals("DO", notes[0].displayName)
        assertEquals(4, notes[0].octave)
        assertEquals("RE#", notes[1].displayName)
        assertEquals("SIb", notes[2].displayName) // "Bb" keeps its flat spelling
    }

    @Test
    fun `claude response with NESSUNA_NOTA fails with explanatory message`() {
        val body = """{"content":[{"type":"text","text":"NESSUNA_NOTA"}]}"""
        val result = ClaudeOmrProcessor.parseResponse(body)
        assertTrue(result.isFailure)
    }

    @Test
    fun `claude malformed response fails instead of crashing`() {
        val result = ClaudeOmrProcessor.parseResponse("not json at all")
        assertTrue(result.isFailure)
    }

    @Test
    fun `openai response with note list is parsed into notes`() {
        val body = """{"choices":[{"message":{"role":"assistant","content":"E4\nF#4"}}]}"""
        val result = OpenAiOmrProcessor.parseResponse(body)
        assertTrue(result.isSuccess, "expected success, got $result")
        val notes = result.getOrThrow()
        assertEquals(2, notes.size)
        assertEquals("MI", notes[0].displayName)
        assertEquals("FA#", notes[1].displayName)
    }

    @Test
    fun `openai empty choices fails gracefully`() {
        val result = OpenAiOmrProcessor.parseResponse("""{"choices":[]}""")
        assertTrue(result.isFailure)
    }

    @Test
    fun `error message is extracted from api error body`() {
        val body = """{"error":{"type":"invalid_request_error","message":"invalid x-api-key"}}"""
        assertEquals("invalid x-api-key", OmrErrorParsing.errorMessage(body, statusCode = 401))
    }

    @Test
    fun `error message falls back to http status for unparseable body`() {
        assertEquals("Errore HTTP 500", OmrErrorParsing.errorMessage("<html>oops</html>", statusCode = 500))
    }
}
