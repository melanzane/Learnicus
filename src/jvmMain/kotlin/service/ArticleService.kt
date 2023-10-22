package service

import org.jsoup.Jsoup

fun fetchUrnIdFromUrl(url: String): String? {
    val doc = Jsoup.connect(url).get()
    val metaTag = doc.selectFirst("meta[name=srf:urn]")
    return metaTag?.attr("content")?.split(":")?.last()
}

