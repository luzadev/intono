package com.notemusicali.scan

import com.notemusicali.music.MusicalNote
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object OpenAiOmrProcessor : OmrProcessor {

    private const val API_URL = "https://api.openai.com/v1/chat/completions"

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
                    header("Authorization", "Bearer $apiKey")
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
            json.parseToJsonElement(responseBody).jsonObject
                .let { it["choices"]?.jsonArray }
                ?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content?.trim()
                ?: return Result.failure(Exception("Risposta vuota dal server"))
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

    private fun buildRequestBody(base64Image: String) = buildJsonObject {
        put("model", "gpt-4o")
        put("max_tokens", 1000)
        put("temperature", 0.1)
        put("messages", buildJsonArray {
            add(buildJsonObject {
                put("role", "user")
                put("content", buildJsonArray {
                    add(buildJsonObject {
                        put("type", "text")
                        put("text", PROMPT)
                    })
                    add(buildJsonObject {
                        put("type", "image_url")
                        put("image_url", buildJsonObject {
                            put("url", "data:image/jpeg;base64,$base64Image")
                            put("detail", "high")
                        })
                    })
                })
            })
        })
    }
}
