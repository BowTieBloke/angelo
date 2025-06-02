package de.arschwasser.angelo.core

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object Downloader {
    private val client: OkHttpClient
        get() = OkHttpClient.Builder()
            .connectTimeout(AppConfig.CONNECT_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.READ_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            .build()

    @Throws(IOException::class)
    fun getBytes(url: String): ByteArray {
        val res = client.newCall(Request.Builder().url(url).build()).execute()
        if (!res.isSuccessful) throw IOException("HTTP ${'$'}{res.code}")
        return res.body!!.bytes()
    }

    fun getString(url: String): String = getBytes(url).decodeToString()
}
