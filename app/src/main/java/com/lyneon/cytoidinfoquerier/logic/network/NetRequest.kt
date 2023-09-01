package com.lyneon.cytoidinfoquerier.logic.network

import com.lyneon.cytoidinfoquerier.logic.model.B30Records
//import com.lyneon.cytoidinfoquerier.logic.model.LevelProfile
import com.lyneon.cytoidinfoquerier.logic.model.PlayerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private val json = Json { ignoreUnknownKeys = true }

object NetRequest {
    suspend fun getPlayerProfile(playerName: String): PlayerProfile {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://services.cytoid.io/profile/$playerName/details")
            .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
            .build()
        val result = withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            try {
                when (response.code) {
                    200 -> response.body?.string()
                    404 -> throw Exception("未找到玩家")
                    else -> throw Exception("Unknown Exception:HTTP response code ${response.code}")
                }
            } finally {
                response.body?.close()
            }
        }
        if (result == null) {
            throw Exception("Request failed")
        } else {
            return json.decodeFromString(result)
        }
    }

    fun getB30RecordsString(playerName: String, count: Int): String {
        val client = OkHttpClient()
        val requestBody = B30Records.getRequestBody(playerName, count)
            .toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://services.cytoid.io/graphql")
            .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
            .post(requestBody)
            .build()
        val response = client.newCall(request).execute()
        val result = try {
            when (response.code) {
                200 -> response.body?.string()
                404 -> throw Exception("未找到玩家")
                else -> throw Exception("Unknown Exception:HTTP response code ${response.code}.${response.body?.string()}")
            }
        } finally {
            response.body?.close()
        }
        if (result == null){
            throw Exception("Unknown Exception:HTTP response code ${response.code}.${response.body?.string()}")
        }else{
            return result
        }
    }

    fun getB30Records(b30RecordsString: String): B30Records =
        json.decodeFromString(b30RecordsString)

 /*   fun getLevelProfile(levelUid: String): LevelProfile {
        val client = OkHttpClient()
        val requestBody = LevelProfile.getRequestBody(levelUid)
            .toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://services.cytoid.io/graphql")
            .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
            .post(requestBody)
            .build()
        val response = client.newCall(request).execute()
        val result = try {
            when (response.code) {
                200 -> response.body?.string()
                404 -> throw Exception("未找到关卡")
                else -> throw Exception("Unknown Exception:HTTP response code ${response.code}")
            }
        } finally {
            response.body?.close()
        }
        if (result == null) {
            throw Exception("Request failed")
        } else {
            return json.decodeFromString(result)
        }
    }*/
}