package com.lyneon.cytoidinfoquerier.util.extension

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.lyneon.cytoidinfoquerier.BaseApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

operator fun String.times(n: Int) = this.repeat(n)

fun String.showToast(duration: Int = Toast.LENGTH_SHORT) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(BaseApplication.context, this@showToast, duration).show()
    }
}

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

@OptIn(ExperimentalContracts::class)
fun String?.isValidCytoidID(
    checkLengthMax: Boolean = true,
    checkLengthMin: Boolean = true
): Boolean {
    contract {
        returns(true) implies (this@isValidCytoidID != null)
    }
    return if (this.isNullOrEmpty()) false else (this.matches("^[a-z0-9_-]*$".toRegex()) && this.length in (if (checkLengthMin) 3 else 0)..(if (checkLengthMax) 16 else Int.MAX_VALUE))
}

fun String.saveIntoClipboard(label: String = "") {
    val clipboardManager =
        BaseApplication.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, this))
}