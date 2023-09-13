package io.beatmaps.bookmarksync

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

val client = HttpClient(Apache) {
    expectSuccess = true

    install(HttpTimeout) {
        socketTimeoutMillis = 5000
        requestTimeoutMillis = 20000
    }
    install(ContentNegotiation) {
        json(json)
    }
}

@Serializable
data class BsaberBookmarksPage(val songs: List<BsaberBookmark>, @SerialName("next_page") val nextPage: Int?)
@Serializable
data class BsaberBookmark(
    val title: String,
    @SerialName("song_key")
    val key: String,
    val hash: String,
    @SerialName("level_author_name")
    val levelAuthorName: String,
    @SerialName("curated_by")
    val curatedBy: String? = null,
    @SerialName("curated_at")
    val curatedAt: String? = null
)

suspend fun getBookmarks(username: String, page: Int, count: Int) =
    client.get("https://bsaber.com/wp-json/bsaber-api/songs/?bookmarked_by=${username}&page=${page}&count=${count}").body<BsaberBookmarksPage>()

suspend fun getAllBookmarks(username: String, cb: (Int) -> Unit = {}) = getAllBookmarksInt(username, cb = cb)
private tailrec suspend fun getAllBookmarksInt(username: String, page: Int = 0, count: Int = 50, prev: List<BsaberBookmark> = listOf(), cb: (Int) -> Unit = {}): List<BsaberBookmark> {
    cb(page)

    val pageObj = getBookmarks(username, page, count)
    val newList = prev.plus(pageObj.songs)

    if (pageObj.nextPage == null) {
        return newList
    }

    return getAllBookmarksInt(username, pageObj.nextPage, count, newList, cb)
}
