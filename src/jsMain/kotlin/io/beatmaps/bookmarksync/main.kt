package io.beatmaps.bookmarksync

import js.objects.jso
import kotlinx.browser.window
import kotlinx.html.id
import react.Props
import react.createElement
import react.dom.client.createRoot
import react.dom.div
import react.dom.nav
import react.fc
import react.router.Outlet
import react.router.dom.RouterProvider
import react.router.dom.createBrowserRouter
import react.useRef
import web.dom.document

fun main() {
    window.onload = {
        document.getElementById("root")?.let { root ->
            createRoot(root).render(createElement(app))
        }
    }
}

val root = fc<Props> {
    // Place global navigation or whatever here
    Outlet()
}

val notFound = fc<Props> {
    div {
        attrs.id = "notfound"
        +"Not found"
    }
}

val app = fc<Props> {
    val appRouter = useRef(
        createBrowserRouter(
            arrayOf(
                jso {
                    path = "/"
                    element = createElement(root)
                    children = arrayOf(
                        jso {
                            index = true
                            element = createElement(homePage)
                        },
                        jso {
                            path = "*"
                            element = createElement(notFound)
                        }
                    )
                }
            )
        )
    )

    RouterProvider {
        attrs.router = appRouter.current!!
    }
}