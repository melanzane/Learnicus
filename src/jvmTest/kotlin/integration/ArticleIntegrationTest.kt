package integration

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import org.example.application.module
import kotlin.test.Test
import kotlin.test.assertEquals

class ArticleIntegrationTest {

    @Test
    fun testArticleJsonUrlEndpoint() {
        val environment = createTestEnvironment {
            module(Application::module)
        }

        with(TestApplicationEngine(environment)) {
            start()

            val originalUrl = "https://www.srf.ch/news/schweiz/abstimmung-kanton-solothurn-luxusgefaengnis-oder-auf-dauer-guenstiger"

            handleRequest(HttpMethod.Get, "/article-json-url?originalUrl=$originalUrl").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("https://www.srf.ch/article/21067631/json", response.content)
            }

            stop(0, 0)
        }
    }
}
