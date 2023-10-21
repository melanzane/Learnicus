package org.example.application

import fetchAndParseRssFeed
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.html.*

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
        routing {
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            static("/static") {
                resources()
            }
            get("/fetchRss") {
                val url = "https://www.srf.ch/news/bnf/rss/1890"
                val jsonString = fetchAndParseRssFeed(url)
                if (jsonString != null) {
                    call.respondText(jsonString, contentType = io.ktor.http.ContentType.Application.Json)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to fetch or parse the RSS feed.")
                }
            }
        }
    }.start(wait = true)
}
