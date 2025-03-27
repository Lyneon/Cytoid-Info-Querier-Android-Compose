package com.lyneon.cytoidinfoquerier.util.extension

import android.net.Uri

fun Uri.contentUriToPath(): String? {
    if (this.scheme != "content") return null
    return when (this.authority) {
        // 系统“文件”
        "com.android.externalstorage.documents" -> {
            this.path?.replace("/document/primary:", "/storage/emulated/0/")
        }

        // MT管理器
        "bin.mt.plus.fp" -> {
            this.path
        }

        // QQ
        "com.tencent.mobileqq" -> {
            val string = this.toString()
            string.substring(string.lastIndexOf("/storage/emulated/0/"))
        }

        else -> null
    }
}