package org.example.application

import config.DatabaseConfig
import database.DatabaseFactory
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlinx.html.*
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarting
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import service.fetchAndParseJsonFeed
import service.fetchAndParseRssFeed
import service.fetchArticlesPeriodically
import service.fetchUrnIdFromUrl
import service.translateArticle

// Define the application scope for coroutines
val applicationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

val logger = LoggerFactory.getLogger("ApplicationLogger")


fun Application.module() {

    routing {
        get("/") {
            call.respondHtml(HttpStatusCode.OK, HTML::index)
        }
        staticResources("/static", "") {
        }

        get("/fetchRss") {
            val url = "https://www.srf.ch/news/bnf/rss/1890"
            val jsonString = fetchAndParseRssFeed(url)
            if (jsonString != null) {
                call.respondText(jsonString, contentType = ContentType.Application.Json)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to fetch or parse the RSS feed.")
            }
        }

        get("/article-json-url") {
            val originalUrl = call.parameters["originalUrl"]
            if (originalUrl != null) {
                val urnId = fetchUrnIdFromUrl(originalUrl)
                if (urnId != null) {
                    val jsonResponseUrl = "https://www.srf.ch/article/$urnId/json"
                    call.respond(HttpStatusCode.OK, jsonResponseUrl)
                } else {
                    call.respond(HttpStatusCode.NotFound, "URN ID not found.")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Please provide an 'originalUrl' parameter.")
            }
        }

        get("/fetchArticleJson") {
            val jsonUrl = call.parameters["jsonUrl"]
            if (jsonUrl != null) {
                val jsonResponse = fetchAndParseJsonFeed(jsonUrl)
                if (jsonResponse != null) {
                    call.respondText(jsonResponse, contentType = ContentType.Application.Json)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to fetch or parse the JSON article.")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Please provide a 'url' parameter.")
            }
        }

        get("/translateArticle") {
            val url = call.parameters["url"]
            if (url != null) {
                val translatedContent = call.translateArticle(url)
                call.respondText(translatedContent, contentType = ContentType.Text.Plain)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Please provide a 'url' parameter.")
            }
        }



    }


}

fun HTML.index() {
    head {
        title("Hello from Learnicus!")
    }
    body {
        div {
            +"Hello from Ktor"
        }
        div {
            id = "root"
        }
        script(src = "/static/Learnicus.js") {}
    }
}

fun main() {
    // Prepare the server but do not start it immediately.
    val server = embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        module() // Your application's module function where routing is configured
    }

    // Subscribe to the ApplicationStarting event to perform initialization tasks.
    server.environment.monitor.subscribe(ApplicationStarting) {
        logger.info("Application is starting, preparing to initialize database...")
        // Launch a coroutine in the applicationScope to initialize the database.
        applicationScope.launch {
            try {
                DatabaseFactory.init(DatabaseConfig())
                logger.info("Initialized database")
            } catch (e: Exception) {
                logger.error("Error initializing the database", e)
            }
        }

        // Also, start the periodic fetching task in the applicationScope.
        applicationScope.fetchArticlesPeriodically()
        logger.info("Started to fetch articles")
    }

    // Start the server and wait for it to finish.
    server.start(wait = true)
}


