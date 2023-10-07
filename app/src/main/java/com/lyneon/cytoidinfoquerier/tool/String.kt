package com.lyneon.cytoidinfoquerier.tool

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.lyneon.cytoidinfoquerier.BaseApplication

operator fun String.times(n: Int) = this.repeat(n)

fun String.showToast(duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(BaseApplication.context, this, duration).show()

fun String.showDialog(
    activity: Activity,
    additionalParameters: (AlertDialog.Builder.() -> AlertDialog.Builder)? = null
): AlertDialog =
    AlertDialog.Builder(activity).apply {
        setMessage(this@showDialog)
        additionalParameters
    }.create()

fun String.isValidCytoidID(): Boolean {
    val regex = "^[a-z0-9_-]*$"
    return this.matches(regex.toRegex())
}

fun String.saveIntoClipboard(label: String = "") {
    val clipboardManager =
        BaseApplication.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, this))
}