package io.beatmaps.bookmarksync

import io.beatmaps.bookmarksync.pages.ActionPageTemplate
import io.beatmaps.bookmarksync.pages.LoginPageTemplate
import io.beatmaps.bookmarksync.pages.MainTemplate
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.oauth
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionTransportTransformerEncrypt
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.util.hex
import java.util.logging.Level
import java.util.logging.Logger

val port = System.getenv("LISTEN_PORT")?.toIntOrNull() ?: 3031
val host = System.getenv("LISTEN_HOST") ?: "127.0.0.1"
val clientId = System.getenv("OAUTH_CLIENT_ID") ?: ""
val clientSecret = System.getenv("OAUTH_CLIENT_SECRET") ?: ""
val tokenUrl = System.getenv("OAUTH_TOKEN_URL") ?: ""
val authUrl = System.getenv("OAUTH_AUTH_URL") ?: ""
val callbackUrl = System.getenv("OAUTH_CALLBACK_URL") ?: ""
val beatsaverUrl = System.getenv("BEATSAVER_URL") ?: ""

private val appLogger = Logger.getLogger("bmio")

fun setupLogging() {
    Logger.getLogger("").level = Level.OFF
    appLogger.level = Level.INFO
}

fun main() {
    setupLogging()

    embeddedServer(Netty, port = port, host = host, module = Application::bookmarksync, watchPaths = listOf("classes", "resources")).start(wait = true)
}

data class UserSession(val state: String, val accessToken: String? = null, val refreshToken: String? = null)

fun Application.bookmarksync() {
    createWorkers()

    install(ContentNegotiation) {
        val kotlinx = KotlinxSerializationConverter(json)

        register(ContentType.Application.Json, kotlinx)
    }

    install(Sessions) {
        val secretEncryptKey = hex(System.getenv("SESSION_ENCRYPT") ?: "f6e7b1c1586ea12a75fe3c588b09e4d2")
        val secretSignKey = hex(System.getenv("SESSION_SIGN") ?: "8b20030ea51ad2e322d197a29ef1")

        cookie<UserSession>("bssync-session") {
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }
    }

    install(Authentication) {
        oauth("BeatSaver") {
            urlProvider = { callbackUrl }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "BeatSaver",
                    authorizeUrl = authUrl,
                    accessTokenUrl = tokenUrl,
                    requestMethod = HttpMethod.Post,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    defaultScopes = listOf("bookmarks"),
                    onStateCreated = { call, state ->
                        call.sessions.set(UserSession(state))
                    }
                )
            }
            client = io.beatmaps.bookmarksync.client
        }
    }

    install(Resources)
    routing {
        apiRoutes()

        get("/") {
            val user = call.sessions.get<UserSession>()

            val template = if (user?.accessToken == null) LoginPageTemplate() else ActionPageTemplate()

            call.respondHtmlTemplate(MainTemplate(template), HttpStatusCode.OK) {
                pageTitle = "Home"
            }
        }

        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/")
        }

        authenticate("BeatSaver") {
            get("/login") {
                call.respondRedirect("/")
            }

            get("/callback") {
                val session = call.sessions.get<UserSession>()!!
                val principal: OAuthAccessTokenResponse.OAuth2 = call.principal()!!

                if (session.state != principal.state) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                call.sessions.set(session.copy(accessToken = principal.accessToken, refreshToken = principal.refreshToken))
                call.respondRedirect("/")
            }
        }

        static {
            resources("static")
        }
    }
}
