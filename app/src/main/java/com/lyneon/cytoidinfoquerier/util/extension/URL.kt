package com.lyneon.cytoidinfoquerier.util.extension

import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

fun URL.toBitmap() = BitmapFactory.decodeStream(
    OkHttpClient().newCall(
        Request.Builder()
            .url(this)
            .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
            .build()
    ).execute().body?.byteStream()
) ?: throw Exception()