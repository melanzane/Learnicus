package service

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

fun fetchAndParseJsonFeed(url: String): String? {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to fetch JSON feed: ${response.message}")
            return null
        }

        val responseBody = response.body?.string() ?: return null

        val jsonArticle = Json.decodeFromString<JsonObject>(responseBody)
        val mappedArticle = mapJsonToArticle(jsonArticle)

        // Convert the mapped article back to a JSON string (if required)
        val jsonString = Json { prettyPrint = true }.encodeToString(mappedArticle)
        return jsonString
    }
}

fun mapJsonToArticle(json: JsonObject): Article? {
    val contentList = mutableListOf<String>()

    val textArray = json["content"]?.jsonObject?.get("text") as? JsonArray ?: return null
    for (textObj in textArray) {
        when (textObj.jsonObject["type"]?.jsonPrimitive?.content) {
            "paragraph", "heading" -> {
                val childrenArray = textObj.jsonObject["children"] as? JsonArray ?: continue
                for (child in childrenArray) {
                    contentList.add(child.jsonObject["text"]?.jsonPrimitive?.content ?: "")
                }
            }
        }
    }

    return Article(
        title = json["info"]?.jsonObject?.get("title")?.jsonPrimitive?.content ?: "",
        subtitle = json["info"]?.jsonObject?.get("headline")?.jsonPrimitive?.content,
        summary = json["info"]?.jsonObject?.get("lead")?.jsonPrimitive?.content ?: "",
        timeDetails = TimeDetails(
            published = json["info"]?.jsonObject?.get("publicationDate")?.jsonPrimitive?.content ?: "",
            updated = json["info"]?.jsonObject?.get("modificationDate")?.jsonPrimitive?.content
        ),
        mainContent = contentList,
        source = json["relatedElements"]?.jsonObject?.get("allElements")?.jsonArray?.get(0)?.jsonObject?.get("articleInfo")?.jsonObject?.get("broadcastReference")?.jsonPrimitive?.content ?: ""
    )
}



@Serializable
data class Article(
    val title: String,
    val subtitle: String? = null,
    val summary: String,
    val timeDetails: TimeDetails,
    val mainContent: List<String>,
    val source: String
)

@Serializable
data class TimeDetails(
    val published: String,
    val updated: String? = null
)
