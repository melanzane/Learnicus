package service

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

fun fetchAndParseRssFeed(url: String): String?{
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to fetch RSS feed: ${response.message}")
            return null
        }

        val inputStream = response.body?.byteStream() ?: return null

        val input = SyndFeedInput()
        val feed = input.build(XmlReader(inputStream))

        println("Feed Title: ${feed.title}")

        val rssFeedItems = feed.entries.map { entry ->
            RssFeedItem(
                title = entry.title,
                link = entry.link,
                description = entry.description.value,
                pubDate = entry.publishedDate.toString(),
                guid = entry.uri
            )
        }

        // Convert the items to JSON
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(ListSerializer(RssFeedItem.serializer()), rssFeedItems)
        return jsonString
    }
}

@Serializable
data class RssFeedItem(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val guid: String
)