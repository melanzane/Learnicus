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

class ArticleJsonIntegrationTest {

    @Test
    fun testFetchingAndParsingOFArticleJson() {

        val environment = createTestEnvironment {
            module(Application::module)
        }

        with(TestApplicationEngine(environment)) {
            start()

            val jsonUrl = "https://www.srf.ch/article/21063854/json"

            handleRequest(HttpMethod.Get, "/fetchArticleJson?jsonUrl=$jsonUrl").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(response.content)
            }

            stop(0, 0)
        }
    }
}
