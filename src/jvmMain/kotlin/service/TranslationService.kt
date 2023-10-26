package service

import io.ktor.client.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val OPENAI_API_ENDPOINT = "https://api.openai.com/v1/engines/davinci/completions"
private const val API_KEY = "secret_key"

suspend fun ApplicationCall.translateArticle(url: String): String {
    val jsonResponse = fetchAndParseJsonFeed(url) ?: return "Failed to fetch or parse the JSON article."
    return try {
        translateWithOpenAI(jsonResponse)
    } catch (e: Exception) {
        "Failed to translate due to: ${e.localizedMessage}"
    }
}

private suspend fun translateWithOpenAI(text: String): String {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000  // 30 seconds
            connectTimeoutMillis = 30000  // 30 seconds
            socketTimeoutMillis = 30000   // 30 seconds
        }
        HttpResponseValidator {
            validateResponse { response ->
                when (response.status.value) {
                    in 300..399 -> throw RedirectResponseException(response, "Redirect response: ${response.status.description}")
                    in 400..499 -> throw ClientRequestException(response, "Client error: ${response.status.description}")
                    in 500..599 -> throw ServerResponseException(response, "Server error: ${response.status.description}")
                }
            }
        }

    }

    return client.use {
        val translationRequest = TranslationRequest(
            prompt = "Maintain the JSON structure and only translate the textual content from German to French: $text",
            max_tokens = 2048
        )

        val response: HttpResponse = it.post(OPENAI_API_ENDPOINT) {
            header("Authorization", "Bearer $API_KEY")
            contentType(ContentType.Application.Json)
            setBody(translationRequest)
        }
        response.bodyAsText()
    }
}

@Serializable
data class TranslationRequest(
    val prompt: String,
    val max_tokens: Int
)
