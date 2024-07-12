package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource

import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.util.extension.setLastCacheTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object LocalDataSource {
    suspend inline fun <reified T> load(cytoidID: String, timeStamp: Long): T =
        withContext(Dispatchers.IO) {
            val localTDir =
                BaseApplication.context.getExternalFilesDir(T::class.simpleName)
            val targetUserTDir = File(localTDir, cytoidID)
            val targetUserTFile = File(targetUserTDir, "$timeStamp.json")
            Json.decodeFromString(targetUserTFile.readText())
        }

    suspend inline fun <reified T> save(cytoidID: String, data: T): Boolean =
        withContext(Dispatchers.IO) {
            val localTDir =
                BaseApplication.context.getExternalFilesDir(T::class.simpleName)
            val currentTimeStamp = System.currentTimeMillis()
            val targetUserTDir = File(localTDir, cytoidID)
            cytoidID.setLastCacheTime<T>(currentTimeStamp)
            val targetUserTFile =
                File(targetUserTDir, "${currentTimeStamp}.json")
            targetUserTFile.writeText(Json.encodeToString(data))
            return@withContext true
        }
}