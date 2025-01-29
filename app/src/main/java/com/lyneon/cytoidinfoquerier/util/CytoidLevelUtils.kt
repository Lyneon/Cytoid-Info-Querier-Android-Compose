package com.lyneon.cytoidinfoquerier.util

import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.data.constant.OkHttpSingleton
import com.lyneon.cytoidinfoquerier.data.model.webapi.LevelRating
import com.lyneon.cytoidinfoquerier.json
import okhttp3.Request

object CytoidLevelUtils {
    fun getLevelCount(): Int {
        val request = Request.Builder()
            .url("https://services.cytoid.io/levels?page=0&limit=1")
            .removeHeader("User-Agent")
            .addHeader("User-Agent", CytoidConstant.clientUA)
            .build()
        val response = OkHttpSingleton.instance.newCall(request).execute()
        return response.headers["X-Total-Entries"]?.toInt() ?: -1
    }

    fun getLevelRating(levelUid: String): LevelRating {
        val request = Request.Builder()
            .url("https://services.cytoid.io/levels/$levelUid/ratings")
            .removeHeader("User-Agent")
            .addHeader("User-Agent", CytoidConstant.clientUA)
            .build()
        val response = OkHttpSingleton.instance.newCall(request).execute()
        if (response.isSuccessful) {
            return json.decodeFromString(
                response.body?.string() ?: throw Exception("Failed to get level rating")
            )
        } else {
            throw Exception("Failed to get level rating")
        }
    }
}