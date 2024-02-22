package com.lyneon.cytoidinfoquerier.logic.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object NetRequest {
    fun getGQLResponseJSONString(GQLQueryString: String): String {
        val response = try {
            OkHttpClient().newCall(
                Request.Builder()
                    .url("https://services.cytoid.io/graphql")
                    .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
                    .post(GQLQueryString.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()
            ).execute()
        } catch (e: IOException) {
            throw e
        }
        val result = try {
            when (response.code) {
                200 -> response.body?.string()
                else -> throw Exception("HTTP response code ${response.code}.${response.body?.string()}")
            }
        } finally {
            response.body?.close()
        }
        if (result == null) {
            throw Exception("Response result is null!HTTP response code ${response.code}.${response.body?.string()}")
        } else {
            return result
        }
    }
}