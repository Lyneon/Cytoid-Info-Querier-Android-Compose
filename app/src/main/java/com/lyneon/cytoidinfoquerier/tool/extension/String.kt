package com.lyneon.cytoidinfoquerier.tool.extension

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
    title: String = "",
    cancellable: Boolean = true,
    additionalParameters: (AlertDialog.Builder.() -> AlertDialog.Builder)? = null
) = AlertDialog.Builder(activity).apply {
    this.setTitle(title)
    this.setMessage(this@showDialog)
    this.setCancelable(cancellable)
    additionalParameters?.let { this.it() }
}.create().show()

fun String.isValidCytoidID() = this.matches("^[a-z0-9_-]*$".toRegex())

fun String.saveIntoClipboard(label: String = "") {
    val clipboardManager =
        BaseApplication.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, this))
}