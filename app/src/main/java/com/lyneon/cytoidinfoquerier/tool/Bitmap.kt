package com.lyneon.cytoidinfoquerier.tool

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore

fun Bitmap.save(contentResolver: ContentResolver, contentValues: ContentValues) {
    val insertUri =
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    insertUri?.let {
        contentResolver.openOutputStream(it).use { outputStream ->
            outputStream?.let { it1 -> this.compress(Bitmap.CompressFormat.PNG, 100, it1) }
        }
    }
}