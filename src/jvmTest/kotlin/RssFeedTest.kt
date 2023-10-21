import kotlin.test.Test
import kotlin.test.assertNotNull

class RssFeedTest {

    @Test
    fun testFetchAndParseRssFeed() {
        val url = "https://www.srf.ch/news/bnf/rss/1890"
        val jsonString = fetchAndParseRssFeed(url)
        assertNotNull(jsonString)
    }
}
