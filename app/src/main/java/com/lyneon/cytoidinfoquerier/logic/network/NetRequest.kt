package com.lyneon.cytoidinfoquerier.logic.network

import com.lyneon.cytoidinfoquerier.logic.model.GQLQueryResponseData
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

val json = Json { ignoreUnknownKeys = true }

object NetRequest {
    fun getGQLResponseJSONString(GQLQueryString: String): String {
        val client = OkHttpClient()
        val requestBody = GQLQueryString.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://services.cytoid.io/graphql")
            .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
            .post(requestBody)
            .build()
        val response = client.newCall(request).execute()
        val result = try {
            when (response.code) {
                200 -> response.body?.string()
                else -> throw Exception("Unknown Exception:HTTP response code ${response.code}.${response.body?.string()}")
            }
        } finally {
            response.body?.close()
        }
        if (result == null) {
            throw Exception("Unknown Exception:response result is null!HTTP response code ${response.code}.${response.body?.string()}")
        } else {
            return result
        }

    }

    fun <QueryType> getGQLObject(GQLQueryString: String): QueryType =
        json.decodeFromString<GQLQueryResponseData<QueryType>>(
            getGQLResponseJSONString(GQLQueryString)
        ).data

    inline fun <reified QueryType> convertGQLResponseJSONStringToObject(GQLResponseJSONString: String) =
        json.decodeFromString<GQLQueryResponseData<QueryType>>(GQLResponseJSONString)
}