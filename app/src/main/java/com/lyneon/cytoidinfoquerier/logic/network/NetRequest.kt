package com.lyneon.cytoidinfoquerier.logic.network

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object NetRequest {
    fun getGQLResponseJSONString(GQLQueryString: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://services.cytoid.io/graphql")
            .removeHeader("User-Agent")
            .addHeader("User-Agent", "CytoidClient/2.1.1")
            .post(GQLQueryString.toRequestBody("application/json".toMediaType()))
            .build()

        val response = try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            Log.e(
                this.javaClass.simpleName,
                "Failed to execute request: ${e.message}. RequestBody: $GQLQueryString"
            )
            throw IOException("Failed to execute request: ${e.message}", e)
        }

        val responseBody = response.body
        responseBody.use {
            return it?.string()
        }
    }
}