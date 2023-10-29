package service

import io.ktor.client.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.ApplicationCall

private const val DEEPL_API_ENDPOINT = "https://api-free.deepl.com/v2/translate"
private const val API_KEY = "my_secret"

suspend fun ApplicationCall.translateArticle(url: String): String {
    val jsonResponse = fetchAndParseJsonFeed(url) ?: return "Failed to fetch or parse the JSON article."
    return try {
        translateWithDeepL(jsonResponse)
    } catch (e: Exception) {
        "Failed to translate due to: ${e.localizedMessage}"
    }
}

private suspend fun translateWithDeepL(text: String): String {
    val client = HttpClient {
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
        val response: HttpResponse = it.post(DEEPL_API_ENDPOINT) {
            header("Authorization", "DeepL-Auth-Key $API_KEY")
            parameter("text", text)
            parameter("target_lang", "FR") // target language is French
        }
        response.bodyAsText()
    }
}
