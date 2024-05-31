package com.lyneon.cytoidinfoquerier.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun Context.writeImageIntoFile(fileName: String, image: Bitmap) {
    this.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
        try {
            image.compress(Bitmap.CompressFormat.JPEG, 100, output)
            output.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}

fun Context.readImageFromFile(fileName: String): Bitmap {
    this.openFileInput(fileName).use { inputStream ->
        return BitmapFactory.decodeStream(inputStream)
    }
}