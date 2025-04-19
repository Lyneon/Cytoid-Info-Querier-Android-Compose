package com.lyneon.cytoidinfoquerier.util

import android.util.Log
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.data.constant.OkHttpSingleton
import com.lyneon.cytoidinfoquerier.data.model.webapi.LevelRating
import com.lyneon.cytoidinfoquerier.json
import okhttp3.Request

object CytoidLevelUtils {
    const val LOG_TAG = "CytoidLevelUtils"

    fun getLevelCount(): Int {
        val request = Request.Builder()
            .url("https://services.cytoid.io/levels?page=0&limit=1")
            .removeHeader("User-Agent")
            .addHeader("User-Agent", CytoidConstant.clientUA)
            .build()
        val response = OkHttpSingleton.instance.newCall(request).execute()
        return response.use { it.headers["X-Total-Entries"]?.toInt() ?: -1 }
    }

    fun getLevelRating(levelUid: String): LevelRating {
        val request = Request.Builder()
            .url("https://services.cytoid.io/levels/$levelUid/ratings")
            .removeHeader("User-Agent")
            .addHeader("User-Agent", CytoidConstant.clientUA)
            .build()

        val response = try {
            OkHttpSingleton.instance.newCall(request).execute()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to fetch level rating data", e)
            return LevelRating()
        }

        return response.use {
            if (!it.isSuccessful) {
                Log.e(LOG_TAG, "Response failed, code ${it.code}")
                return LevelRating()
            }
            it.body?.let { body ->
                try {
                    json.decodeFromString<LevelRating>(body.string())
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Failed to parse json", e)
                    return LevelRating()
                }
            } ?: LevelRating().also {
                Log.e(LOG_TAG, "Response body is null")
            }
        }
    }
}