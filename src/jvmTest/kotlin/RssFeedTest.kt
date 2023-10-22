import org.junit.jupiter.api.Test
import service.fetchAndParseRssFeed
import org.junit.jupiter.api.Assertions.assertNotNull
class RssFeedTest {

    @Test
    fun testFetchAndParseRssFeed() {
        val url = "https://www.srf.ch/news/bnf/rss/1890"
        val jsonString = fetchAndParseRssFeed(url)
        assertNotNull(jsonString)
    }
}
