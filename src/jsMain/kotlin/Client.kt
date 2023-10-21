import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import react.create
import react.dom.client.createRoot
import kotlinx.serialization.json.Json

suspend fun fetchFeedItems(): List<FeedItem> {
    val response = window.fetch("/fetchRss", RequestInit(
        method = "GET"
    )).await()

    if (!response.ok) {
        throw Exception("Network request failed with status ${response.status}: ${response.statusText}")
    }

    val text = response.text().await()
    return Json.decodeFromString(text)
}

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    // Start a coroutine to fetch real data
    GlobalScope.launch {
        try {
            val items = fetchFeedItems()

            val feedList = FeedList.create {
                this.items = items
            }
            createRoot(container).render(feedList)
        } catch (error: Throwable) {
            console.error("Failed to fetch RSS feed items:", error)
        }
    }
}
