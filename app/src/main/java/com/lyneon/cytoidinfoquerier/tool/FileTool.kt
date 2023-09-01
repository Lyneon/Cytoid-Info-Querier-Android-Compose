package com.lyneon.cytoidinfoquerier.tool

import android.content.Context
import android.graphics.Bitmap
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter


fun Context.saveStringFile(fileName:String, fileContent:String){
    val outputStream = this.openFileOutput(fileName,Context.MODE_PRIVATE)
    val writer = BufferedWriter(OutputStreamWriter(outputStream))
    writer.use {
        it.write(fileContent)
    }
}

fun Context.loadStringFile(fileName: String):String{
    val fileContent = StringBuilder()
    val inputStream = this.openFileInput(fileName)
    val reader = BufferedReader(InputStreamReader(inputStream))
    reader.use {
        reader.forEachLine {
            fileContent.append(it)
        }
    }
    return  fileContent.toString()
}

fun Context.saveImageFile(fileName: String,image:Bitmap){
    try {
        val out = this.openFileOutput(fileName,Context.MODE_PRIVATE)
        image.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}