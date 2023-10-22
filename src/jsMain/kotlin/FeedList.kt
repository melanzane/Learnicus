import kotlinx.browser.document
import kotlinx.serialization.Serializable

@Serializable
data class FeedItem(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val guid: String
)

fun renderFeedList(container: dynamic, items: List<FeedItem>) {
    val ul = document.createElement("ul")
    items.forEach { item ->
        val li = document.createElement("li")
        val a = document.createElement("a")
        a.textContent = item.title
        a.setAttribute("href", item.link)
        li.appendChild(a)
        ul.appendChild(li)
    }
    container.appendChild(ul)
}
