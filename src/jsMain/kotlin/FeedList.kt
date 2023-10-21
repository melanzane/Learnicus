import kotlinx.serialization.Serializable
import react.*
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ul

external interface FeedListProps : Props {
    var items: List<FeedItem>
}

@Serializable
data class FeedItem(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val guid: String
)

val FeedList = FC<FeedListProps> { props ->
    ul {
        props.items.forEach { item ->
            li {
                +"${item.title}: ${item.link}"
            }
        }
    }
}
