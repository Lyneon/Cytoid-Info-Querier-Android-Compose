package com.lyneon.cytoidinfoquerier.tool

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

fun URL.toBitmap():Bitmap {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(this)
        .removeHeader("User-Agent").addHeader("User-Agent","CytoidClient/2.1.1")
        .build()
    val response = client.newCall(request).execute()
    return BitmapFactory.decodeStream(response.body?.byteStream())
}