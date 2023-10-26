package org.example.application

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlinx.html.*
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import service.fetchAndParseJsonFeed
import service.fetchAndParseRssFeed
import service.fetchUrnIdFromUrl
import service.translateArticle

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
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        module()
    }.start(wait = true)
}
