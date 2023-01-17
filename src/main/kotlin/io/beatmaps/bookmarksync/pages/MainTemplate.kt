package io.beatmaps.bookmarksync.pages

import io.ktor.server.html.Placeholder
import io.ktor.server.html.Template
import io.ktor.server.html.TemplatePlaceholder
import io.ktor.server.html.insert
import kotlinx.html.BODY
import kotlinx.html.HEAD
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.lang
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.styleLink
import kotlinx.html.title

class MainTemplate(private val body: Template<BODY>) : Template<HTML> {
    private val bodyPlaceholder = TemplatePlaceholder<Template<BODY>>()
    val headElements = Placeholder<HEAD>()
    var pageTitle = ""

    override fun HTML.apply() {
        lang = "en"
        head {
            insert(headElements)
            title { +pageTitle }
            styleLink("/main.css")
            styleLink("/bootstrap.min.css")
            styleLink("https://use.fontawesome.com/releases/v5.15.4/css/all.css")
            meta("theme-color", "#375a7f")
            meta("viewport", "width=device-width, min-width=575")
            meta("Description", "Bookmark Sync")
        }
        body {
            insert(body, bodyPlaceholder)
            script(src = "/main.js") {}
            script(src = "https://cdn.jsdelivr.net/npm/axios@1.1.2/dist/axios.min.js") {}
        }
    }
}
