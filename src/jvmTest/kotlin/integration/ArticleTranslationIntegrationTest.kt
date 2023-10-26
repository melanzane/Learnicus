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
import kotlin.test.assertNotNull

class ArticleTranslationIntegrationTest {

    @Test
    fun testFetchingAndParsingOFArticleJson() {

        val environment = createTestEnvironment {
            module(Application::module)
        }

        with(TestApplicationEngine(environment)) {
            start()

            val jsonUrl = "https://www.srf.ch/article/21070721/json"

            handleRequest(HttpMethod.Get, "/translateArticle?url=$jsonUrl").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(response.content)
            }

            stop(0, 0)
        }
    }
}
