package com.lyneon.cytoidinfoquerier.logic.network

import com.lyneon.cytoidinfoquerier.model.webapi.Profile
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

val json = Json { ignoreUnknownKeys = true }

object NetRequest {
    fun getGQLResponseJSONString(GQLQueryString: String): String {
        val response = OkHttpClient().newCall(
            Request.Builder()
                .url("https://services.cytoid.io/graphql")
                .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
                .post(GQLQueryString.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
        ).execute()
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

    fun getProfile(cytoidID: String): Profile {
        val response = OkHttpClient().newCall(
            Request.Builder()
                .url("https://services.cytoid.io/profile/$cytoidID/details")
                .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
                .build()
        ).execute()
        val result = try {
            when (response.code) {
                200 -> response.body?.string()
                else -> throw Exception("Unknown Exception:HTTP response code ${response.code}.${response.body?.string()}")
            }
        } finally {
            response.body?.close()
        }
        return if (result == null) {
            throw Exception("Unknown Exception:response result is null!HTTP response code ${response.code}.${response.body?.string()}")
        } else {
            json.decodeFromString(result)
        }
    }
}