package com.lyneon.cytoidinfoquerier.data.model.webapi

import com.lyneon.cytoidinfoquerier.json
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@Serializable
data class Comment(
    val content: String,
    val date: String,
    val owner: Owner
) {
    @Serializable
    data class Owner(
        val uid: String,
        val avatar: Avatar
    ) {
        @Serializable
        data class Avatar(
            val original: String,
            val small: String,
            val medium: String,
            val large: String
        )
    }

    companion object {
        fun get(id: String): ArrayList<Comment> {
            val response = try {
                OkHttpClient().newCall(
                    Request.Builder()
                        .url("https://services.cytoid.io/threads/profile/$id")
                        .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
                        .build()
                ).execute()
            } catch (e: IOException) {
                throw e
            }
            val result = try {
                when (response.code) {
                    200 -> response.body?.string()
                    else -> throw Exception("Unknown Exception: HTTP response code is${response.code}")
                }
            } finally {
                response.body?.close()
            }
            return if (result == null) {
                throw Exception("Response body is null!HTTP response code is ${response.code}")
            } else {
                json.decodeFromString(result)
            }
        }
    }
}


