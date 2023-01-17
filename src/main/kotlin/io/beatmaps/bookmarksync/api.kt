package io.beatmaps.bookmarksync

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Resource("/state/{uuid}")
class GetState(val uuid: String)

@Resource("/start")
class StartJob

@Serializable
data class TokenResponse(@SerialName("access_token") val accessToken: String, @SerialName("refresh_token") val refreshToken: String)

private suspend fun refreshToken(
    client: HttpClient,
    refreshToken: String
) = client.submitForm(
    tokenUrl,
    Parameters.build {
        append("grant_type", "refresh_token")
        append("refresh_token", refreshToken)
        append("client_id", clientId)
        append("client_secret", clientSecret)
    }
).body<TokenResponse>()

fun Routing.apiRoutes() {
    get<GetState> { req ->
        getProgress(req.uuid)?.let { progress ->
            call.respond(progress)
        } ?: run {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    post<StartJob> {
        val user = call.sessions.get<UserSession>()
        val bsaberUser = call.receiveText()

        if (user?.accessToken == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val authWorking = httpTry {
            client.get("${beatsaverUrl}/bookmarks/0?pageSize=1") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${user.accessToken}")
                }
            }
        }

        if (authWorking == null) {
            try {
                val newTokens = refreshToken(client, user.refreshToken ?: throw Exception("Missing refresh token"))
                call.sessions.set(user.copy(accessToken = newTokens.accessToken, refreshToken = newTokens.refreshToken))
            } catch (e: Exception) {
                call.sessions.clear<UserSession>()
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
        }

        val jobid = SyncJob(bsaberUser, user.accessToken).queue()
        call.respondText(jobid)
    }
}
