package com.lyneon.cytoidinfoquerier.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

fun Context.writeStringIntoFile(fileName: String, fileContent: String) {
    val outputStream = this.openFileOutput(fileName, Context.MODE_PRIVATE)
    val writer = BufferedWriter(OutputStreamWriter(outputStream))
    writer.use {
        it.write(fileContent)
    }
    outputStream.close()
}

fun Context.readStringFromFile(fileName: String): String {
    val fileContent = StringBuilder()
    val inputStream = this.openFileInput(fileName)
    val reader = BufferedReader(InputStreamReader(inputStream))
    reader.use {
        reader.forEachLine {
            fileContent.append(it)
        }
    }
    inputStream.close()
    return fileContent.toString()
}

fun Context.writeImageIntoFile(fileName: String, image: Bitmap) {
    val output = this.openFileOutput(fileName, Context.MODE_PRIVATE)
    try {
        image.compress(Bitmap.CompressFormat.JPEG, 100, output)
        output.flush()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    } finally {
        output.close()
    }
}

fun Context.readImageFromFile(fileName: String): Bitmap {
    val inputStream = this.openFileInput(fileName)
    val image = BitmapFactory.decodeStream(inputStream)
    inputStream.close()
    return image
}