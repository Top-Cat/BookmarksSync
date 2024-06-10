package io.beatmaps.bookmarksync.pages

import io.ktor.server.html.Template
import kotlinx.html.BODY
import kotlinx.html.div
import kotlinx.html.id

class ReactPageTemplate : Template<BODY> {
    override fun BODY.apply() {
        div {
            id = "root"
        }
    }
}
