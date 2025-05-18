package com.lyneon.cytoidinfoquerier.util

import com.lyneon.cytoidinfoquerier.BaseApplication
import java.io.File

object CytoidIdAutoFillUtils {

    val saveFile = File(BaseApplication.context.filesDir, "saved_cytoid_ids")

    fun getSavedCytoidIds(): List<String> = if (!saveFile.exists()) {
        emptyList()
    } else {
        saveFile.readLines().filter { it.isNotBlank() }
    }

    fun saveCytoidId(cytoidId: String) {
        if (!saveFile.exists()) {
            saveFile.createNewFile()
            saveFile.writeText("$cytoidId\n")
            return
        } else if (!getSavedCytoidIds().contains(cytoidId)) {
            saveFile.appendText("$cytoidId\n")
        }
    }

    fun forgetCytoidId(cytoidId: String) {
        if (!saveFile.exists()) {
            return
        }
        saveFile.writeText(getSavedCytoidIds().filter { it != cytoidId }.joinToString("\n"))
    }

    fun clearAllCytoidIds() {
        if (saveFile.exists()) {
            saveFile.writeText("")
        }
    }
}