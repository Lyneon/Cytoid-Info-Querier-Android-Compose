package com.lyneon.cytoidinfoquerier.service

import android.util.Log
import com.lyneon.cytoidinfoquerier.IFileService
import kotlin.system.exitProcess

class FileService : IFileService.Stub() {

    override fun destroy() {
        Log.i("FileService", "destroy")
        exitProcess(0)
    }

    override fun copyFileTo(sourceFilePath: String?, targetDirectoryPath: String?): Boolean {
        if (sourceFilePath.isNullOrEmpty() || targetDirectoryPath.isNullOrEmpty()) {
            return false
        }
        Log.i("FileService", "copy $sourceFilePath to $targetDirectoryPath")

        val runtime = Runtime.getRuntime()
        val process = runtime.exec("cp $sourceFilePath $targetDirectoryPath")
        val exitValue = process.waitFor()
        return exitValue == 0
    }
}