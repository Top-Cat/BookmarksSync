package io.beatmaps.bookmarksync.pages

import io.ktor.server.html.Template
import kotlinx.html.BODY
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.main
import kotlinx.html.p
import kotlinx.html.style

class ActionPageTemplate : Template<BODY> {
    override fun BODY.apply() {
        main("container text-center") {
            form {
                div("form-group mb-4") {
                    label("form-label") {
                        +"BeastSaber username"
                    }
                    input(InputType.text, classes = "form-control") {
                        id = "username"
                        placeholder = "Enter bsaber username"
                    }
                }
                button(classes = "btn btn-primary") {
                    +"Sync"
                }
                a("/logout", classes = "btn btn-danger ms-2") {
                    +"Logout"
                }
            }
            div("job") {
                id = "job"
                p("fs-5 mb-2") {
                    id = "job-wait"
                    +"Waiting for job to start"
                }
                div {
                    id = "job-dl"
                    p("fs-5 mt-4 mb-2") {
                        +"Downloading maps from BeastSaber"
                    }
                    div("progress") {
                        div("progress-bar progress-bar-striped progress-bar-animated") {
                            id = "dl-progress"
                            style = "width: 75%"
                        }
                    }
                }
                div {
                    id = "job-up"
                    p("fs-5 mt-4 mb-2") {
                        +"Adding bookmarks on BeatSaver"
                    }
                    div("progress") {
                        div("progress-bar progress-bar-striped progress-bar-animated bg-info") {
                            id = "up-progress"
                            style = "width: 25%"
                        }
                    }
                }
                p("fs-5 mb-2") {
                    id = "job-done"
                    +"Bookmarks synced!"
                }
            }
        }
    }
}
