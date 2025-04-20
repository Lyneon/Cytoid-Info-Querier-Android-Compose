package com.lyneon.cytoidinfoquerier.util.extension

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.data.constant.okHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URL

fun URL.toBitmap(): Bitmap = okHttpClient.newCall(
    Request.Builder()
        .url(this)
        .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
        .build()
).execute().use { response ->
    val body = response.body ?: throw IOException("Response body is null")
    val inputStream = body.byteStream()
    BitmapFactory.decodeStream(inputStream) ?: throw IOException("Failed to decode bitmap stream")
}

fun URL.openInBrowser() {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = this@openInBrowser.toString().toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    BaseApplication.context.startActivity(intent)
}