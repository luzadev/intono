package com.notemusicali.scan

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Shared HTTP plumbing for the OMR providers. */
internal object OmrHttp {
    fun createClient(): HttpClient = HttpClient {
        install(HttpTimeout) {
            // Vision requests on a large image can be slow, but must not hang forever
            requestTimeoutMillis = 90_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 90_000
        }
    }
}

/** Extracts a human-readable message from a provider error body (Anthropic and OpenAI share the shape). */
object OmrErrorParsing {
    private val json = Json { ignoreUnknownKeys = true }

    fun errorMessage(body: String, statusCode: Int): String = try {
        json.parseToJsonElement(body).jsonObject["error"]
            ?.jsonObject?.get("message")?.jsonPrimitive?.content
            ?: "Errore HTTP $statusCode"
    } catch (_: Exception) {
        "Errore HTTP $statusCode"
    }
}
