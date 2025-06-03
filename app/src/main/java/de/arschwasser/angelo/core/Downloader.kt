package de.arschwasser.angelo.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object Downloader {

    private val client = OkHttpClient()

    /** Blocking byte download – MUST run off-UI thread */
    suspend fun getBytes(url: String): ByteArray =
        withContext(Dispatchers.IO) {          // ⬅ move work to I/O pool
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
                response.body!!.bytes()
            }
        }

    /** Convenience wrapper for text download */
    suspend fun getString(url: String): String =
        getBytes(url).decodeToString()
}
