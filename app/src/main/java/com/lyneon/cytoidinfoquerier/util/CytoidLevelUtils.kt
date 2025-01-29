package com.lyneon.cytoidinfoquerier.util

import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.data.constant.OkHttpSingleton
import okhttp3.Request

object CytoidLevelUtils {
    fun getLevelCount(): Int {
        val request = Request.Builder()
            .url("https://services.cytoid.io/levels?page=0&limit=1")
            .removeHeader("User-Agent")
            .addHeader("User-Agent", CytoidConstant.clientUA)
            .build()
        val response = OkHttpSingleton.instance.newCall(request).execute()
        return response.headers.get("X-Total-Entries")?.toInt() ?: -1
    }
}