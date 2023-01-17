package io.beatmaps.bookmarksync.pages

import io.ktor.server.html.Template
import kotlinx.html.BODY
import kotlinx.html.a
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.main
import kotlinx.html.p

class LoginPageTemplate : Template<BODY> {
    override fun BODY.apply() {
        main("container text-center") {
            div {
                id = "login"
                p("fs-5 mb-4") {
                    +"You can use this app to copy bookmarks from a BeastSaber account to BeatSaver."
                    br {}
                    +"Authenticate with your BeatSaver account, then we'll ask you for a BeastSaber username to copy bookmarks from."
                }
                a("/login", classes = "btn btn-primary") {
                    +"Login to get started!"
                }
            }
        }
    }
}
