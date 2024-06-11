package io.beatmaps.bookmarksync

import io.beatmaps.bookmarksync.pages.MainTemplate
import io.beatmaps.bookmarksync.pages.ReactPageTemplate
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.EntityTagVersion
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.http.content.CompressedFileType
import io.ktor.server.http.content.StaticContentConfig
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.path
import io.ktor.server.resources.Resources
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

val port = System.getenv("LISTEN_PORT")?.toIntOrNull() ?: 3031
val host = System.getenv("LISTEN_HOST") ?: "127.0.0.1"
val exportFolder = System.getenv("EXPORT_DIR")?.let { File(it) }

private val appLogger = Logger.getLogger("bmio")

fun setupLogging() {
    Logger.getLogger("").level = Level.OFF
    appLogger.level = Level.INFO
}

fun main() {
    setupLogging()

    embeddedServer(Netty, port = port, host = host, module = Application::bookmarksync, watchPaths = listOf("classes", "resources")).start(wait = true)
}

fun Application.bookmarksync() {
    install(ContentNegotiation) {
        val kotlinx = KotlinxSerializationConverter(json)

        register(ContentType.Application.Json, kotlinx)
    }

    install(ConditionalHeaders) {
        version { call, _ ->
            val path = call.request.path()
            when {
                path.startsWith("/static") -> listOf(EntityTagVersion(Etag.docker))
                else -> emptyList()
            }
        }
    }

    install(Resources)
    routing {
        get("/") {
            val template = ReactPageTemplate()

            call.respondHtmlTemplate(MainTemplate(template), HttpStatusCode.OK) {
                pageTitle = "Home"
            }
        }

        val cacheSettings: StaticContentConfig<*>.() -> Unit = {
            preCompressed(CompressedFileType.GZIP)

            cacheControl { _ ->
                listOf(CacheControl.MaxAge(60 * 60))
            }
        }

        staticResources("/static", "static") {
            cacheSettings()
        }
        exportFolder?.let {
            staticFiles("/playlist", it) { cacheSettings() }
        }
    }
}
