package io.beatmaps.bookmarksync

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.net.URISyntaxException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.forEachIndexed
import kotlin.collections.set

enum class JobStatus {
    WAITING, DOWNLOAD, UPLOAD, ERROR, COMPLETE
}

private val queue = Channel<SyncJob>(1000)
private val lookup = ConcurrentHashMap<String, SyncJob>()

fun getProgress(uuid: String) = lookup[uuid]?.progress
@Serializable
data class JobProgress(val status: JobStatus = JobStatus.WAITING, val progress: Float = 0f)

class SyncJob(val bsaberUsername: String, val accessToken: String) {
    var progress = JobProgress()

    fun queue() = UUID.randomUUID().toString().also { uuid ->
        queue.trySend(this)
        lookup[uuid] = this
    }

    fun setStatus(status: JobStatus) {
        progress = progress.copy(status = status)
    }

    fun setProgress(f: Float) {
        progress = progress.copy(progress = f)
    }
}

@Serializable
data class BookmarkRequest(val key: String? = null, val hash: String? = null, val bookmarked: Boolean)

@OptIn(DelicateCoroutinesApi::class)
fun createWorkers() = repeat(3) {
    GlobalScope.launch {
        while (true) {
            val job = queue.receive()

            try {
                job.setStatus(JobStatus.DOWNLOAD)

                val bookmarks = getAllBookmarks(job.bsaberUsername) {
                    job.setProgress(it.toFloat())
                }

                job.setStatus(JobStatus.UPLOAD)
                bookmarks.forEachIndexed { idx, bookmark ->
                    job.setProgress((idx + 1f) / bookmarks.size)

                    httpTry {
                        client.post("${beatsaverUrl}/bookmark") {
                            contentType(ContentType.Application.Json)
                            setBody(BookmarkRequest(hash = bookmark.hash, bookmarked = true))
                            headers {
                                append(HttpHeaders.Authorization, "Bearer ${job.accessToken}")
                            }
                        }
                    }

                    delay(100)
                }

                job.setStatus(JobStatus.COMPLETE)
            } catch (e: Exception) {
                e.printStackTrace()
                job.setStatus(JobStatus.ERROR)
            }
        }
    }
}

suspend fun <T> httpTry(block: suspend () -> T) =
    try {
        block()
    } catch (e: ClientRequestException) {
        null // 4xx response
    } catch (e: URISyntaxException) {
        null // Bad characters in hash, likely only locally
    } catch (e: JsonConvertException) {
        e.printStackTrace()
        null // Bad json, scoresaber schema changed?
    }
