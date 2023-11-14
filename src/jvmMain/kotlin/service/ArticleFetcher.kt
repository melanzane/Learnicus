package service

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import database.DatabaseFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import models.ArticleJson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("ApplicationLogger")

fun CoroutineScope.fetchArticlesPeriodically() {
    launch(Dispatchers.IO) {  // Use the IO dispatcher for network operations
        while (isActive) { // Use isActive from the coroutine's context
            try {
                val rssUrl = "https://www.srf.ch/news/bnf/rss/1890" // The RSS feed URL
                val rssJsonString: String? = fetchAndParseRssFeed(rssUrl)
                if (rssJsonString != null) {
                    val json = Json { ignoreUnknownKeys = true } // Create a Json instance with configuration if needed
                    val rssFeedItemList: List<RssFeedItem> = json.decodeFromString(ListSerializer(RssFeedItem.serializer()), rssJsonString)
                    val guidsList: List<String> = rssFeedItemList.map { it.guid }
                    val jsonUrlList: List<String> = fetchUrnUrls(guidsList)
                    fetchAndStoreArticles(jsonUrlList)
                } else {
                    logger.error("RSS feed could not be fetched or parsed")
                }
            } catch (e: Exception) {
                logger.error("Error fetching articles: ${e.localizedMessage}")
            }
            delay(30 * 60 * 1000) // Wait for 30 minutes before the next iteration
        }
    }
}

suspend fun fetchAndStoreArticles(jsonUrlList: List<String>) {
    coroutineScope { // Use coroutineScope to wait for all the launched jobs
        jsonUrlList.forEach { jsonUrl ->
            launch {
                try {
                    val jsonContent = fetchJsonContent(jsonUrl)
                    if (jsonContent != null) {
                        // Use the URL as the sourceId and the JSON content as the jsonData.
                        val filter = eq("sourceId", jsonUrl)
                        val update = Updates.set("jsonData", jsonContent)
                        val result = DatabaseFactory.database.getCollection<ArticleJson>("articlejson")
                            .updateOne(filter, update, UpdateOptions().upsert(true))

                        if (result.upsertedId != null) {
                            logger.info("Upserted article with sourceId: $jsonUrl")
                        } else {
                            logger.info("Updated article with sourceId: $jsonUrl")
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error upserting article into MongoDB: ${e.localizedMessage}")
                }
            }
        }
    }
}






suspend fun fetchUrnUrls(guidList: List<String>): List<String> {
    // This will hold all the URLs you fetch.
    val urnUrls = mutableListOf<String>()

    coroutineScope { // Use coroutineScope to wait for all the launched jobs
        guidList.forEach { originalUrl ->
            launch {
                val urnId = fetchUrnIdFromUrl(originalUrl)
                if (urnId != null) {
                    val jsonResponseUrl = "https://www.srf.ch/article/$urnId/json"
                    urnUrls.add(jsonResponseUrl)
                }
            }
        }
    }

    return urnUrls
}



suspend fun fetchJsonContent(url: String): String? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to fetch JSON feed: ${response.message}")
            return@withContext null
        }

        val responseBody = response.body?.string() ?: return@withContext null

        // Decode the JSON string to a JsonObject
        val jsonArticle = Json.decodeFromString<JsonObject>(responseBody)

        // Convert the mapped article back to a JSON string (if required)
        Json { prettyPrint = true }.encodeToString(jsonArticle)
    }
}