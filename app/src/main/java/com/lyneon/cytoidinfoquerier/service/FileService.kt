package com.lyneon.cytoidinfoquerier.service

import android.util.Log
import com.lyneon.cytoidinfoquerier.IFileService
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
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
        val process =
            runtime.exec("cp ${sourceFilePath.toSafeShellPath()} ${targetDirectoryPath.toSafeShellPath()}")
        val exitValue = process.waitFor()
        return exitValue == 0
    }

    override fun readFile(filePath: String?): String {
        if (filePath.isNullOrEmpty()) {
            return ""
        }

        val process = Runtime.getRuntime().exec("cat ${filePath.toSafeShellPath()}")

        val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
        val error = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }

        return when (process.waitFor()) {
            0 -> output.trim()
            else -> throw Exception("Failed to read file: ${error.ifEmpty { "Unknown error" }}")
        }
    }

    override fun listFiles(directoryPath: String?): Array<String> {
        if (directoryPath.isNullOrEmpty()) {
            return arrayOf()
        }

        val process = Runtime.getRuntime().exec("ls -A ${directoryPath.toSafeShellPath()}")

        val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
        val error = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }

        return when (process.waitFor()) {
            0 -> {
                output.lines()
                    .filter { it.isNotBlank() }
                    .toList()
                    .toTypedArray()
            }

            else -> throw Exception("Failed to list files: ${error.ifEmpty { "Unknown error" }}")
        }
    }

    override fun readBytes(filePath: String?): ByteArray {
        if (filePath.isNullOrEmpty()) {
            return byteArrayOf()
        }
        val file = File(filePath)
        return try {
            file.readBytes()
        } catch (e: Exception) {
            Log.e("FileService", "Failed to read bytes from file: ${e.message}")
            byteArrayOf()
        }
    }

    override fun exists(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        val process = Runtime.getRuntime().exec("test -e ${path.toSafeShellPath()}")
        return process.waitFor() == 0
    }

    override fun isFile(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        val process = Runtime.getRuntime().exec("test -f ${path.toSafeShellPath()}")
        return process.waitFor() == 0
    }

    override fun isDirectory(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        val process = Runtime.getRuntime().exec("test -d ${path.toSafeShellPath()}")
        return process.waitFor() == 0
    }

    private fun safeShellPath(rawPath: String?): String {
        if (rawPath.isNullOrBlank()) return "''"

        // 转义路径中的单引号（将'替换为'\''
        val escaped = rawPath.replace("'", "'\"'\"'")

        // 用单引号包裹整个路径
        return "'$escaped'"
    }

    private fun String.toSafeShellPath(): String = safeShellPath(this)
}