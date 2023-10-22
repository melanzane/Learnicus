import org.junit.jupiter.api.Test
import service.fetchUrnIdFromUrl
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ArticleServiceTest {

    @Test
    fun testFetchUrnIdFromUrl() {
        val url = "https://www.srf.ch/news/schweiz/abstimmung-kanton-solothurn-luxusgefaengnis-oder-auf-dauer-guenstiger"
        val urn = fetchUrnIdFromUrl(url)
        assertNotNull(urn)
        assertEquals(urn, "21067631")
    }

}