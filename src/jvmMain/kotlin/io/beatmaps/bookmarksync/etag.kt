package io.beatmaps.bookmarksync

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

object Etag {
    val docker by lazy {
        val dockerHash = File("/etc/hostname").let {
            if (it.exists()) {
                it.readText()
            } else {
                ""
            }
        }
        md5(dockerHash)
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(input.toByteArray())

        val fx = "%0" + md.digestLength * 2 + "x"
        return String.format(fx, BigInteger(1, md.digest()))
    }
}
