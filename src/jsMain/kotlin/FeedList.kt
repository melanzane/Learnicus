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

        val firstLink = document.createElement("a")
        firstLink.textContent = item.title
        firstLink.setAttribute("href", item.link)
        li.appendChild(firstLink)

        val breakCharacter = document.createElement("br")
        li.appendChild(breakCharacter)

        val secondLink = document.createElement("a")
        secondLink.textContent = "translate"
        secondLink.setAttribute("href", item.link)
        li.appendChild(secondLink)

        ul.appendChild(li)
    }
    container.appendChild(ul)
}
