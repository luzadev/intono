package com.notemusicali.scan

import com.notemusicali.music.MusicalNote
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object ClaudeOmrProcessor : OmrProcessor {

    private const val API_URL = "https://api.anthropic.com/v1/messages"

    private val PROMPT = """
        Analizza questa immagine di spartito musicale.
        Elenca SOLO le note in ordine di apparizione, una per riga, nel formato:
        LETTERA OTTAVA
        Usa la notazione anglosassone: C D E F G A B (con # o b per alterazioni).
        Esempio:
        C4
        D#5
        Bb3
        E4

        Ignora pause, dinamiche, legature e qualsiasi altro simbolo non-nota.
        Se non riesci a identificare note, rispondi esattamente: NESSUNA_NOTA
    """.trimIndent()

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyze(imageBase64: String, apiKey: String): Result<List<MusicalNote>> {
        return try {
            val client = OmrHttp.createClient()
            try {
                val requestBody = buildRequestBody(imageBase64)

                val response = client.post(API_URL) {
                    header("x-api-key", apiKey)
                    header("anthropic-version", "2023-06-01")
                    contentType(ContentType.Application.Json)
                    setBody(requestBody.toString())
                }

                val responseBody = response.bodyAsText()

                if (response.status.value !in 200..299) {
                    return Result.failure(Exception(OmrErrorParsing.errorMessage(responseBody, response.status.value)))
                }

                parseResponse(responseBody)
            } finally {
                client.close()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    internal fun parseResponse(responseBody: String): Result<List<MusicalNote>> {
        val content = try {
            val responseJson = json.parseToJsonElement(responseBody).jsonObject
            val contentArray = responseJson["content"]?.jsonArray
                ?: return Result.failure(Exception("Nessun contenuto nella risposta"))

            contentArray
                .map { it.jsonObject }
                .firstOrNull { it["type"]?.jsonPrimitive?.content == "text" }
                ?.get("text")?.jsonPrimitive?.content?.trim()
                ?: return Result.failure(Exception("Nessun testo nella risposta"))
        } catch (e: Exception) {
            return Result.failure(Exception("Risposta non valida dal server"))
        }

        if (content == "NESSUNA_NOTA") {
            return Result.failure(Exception("Nessuna nota riconosciuta nell'immagine"))
        }

        val notes = NoteParser.parseNotes(content)
        return if (notes.isEmpty()) {
            Result.failure(Exception("Nessuna nota valida trovata nella risposta"))
        } else {
            Result.success(notes)
        }
    }

    private fun buildRequestBody(base64Image: String): JsonObject {
        return buildJsonObject {
            // claude-sonnet-4-20250514 is deprecated (retires June 2026); claude-sonnet-5 is its replacement
            put("model", "claude-sonnet-5")
            put("max_tokens", 1024)
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", buildJsonArray {
                        add(buildJsonObject {
                            put("type", "image")
                            put("source", buildJsonObject {
                                put("type", "base64")
                                put("media_type", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                        add(buildJsonObject {
                            put("type", "text")
                            put("text", PROMPT)
                        })
                    })
                })
            })
        }
    }
}
