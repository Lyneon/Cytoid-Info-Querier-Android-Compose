package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource

import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.json
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileComment
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.util.extension.setLastCacheTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import java.io.File

object LocalDataSource {
    /*
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
            if (!targetUserTDir.exists()) targetUserTDir.mkdirs()
            val targetUserTFile =
                File(targetUserTDir, "${currentTimeStamp}.json")
            targetUserTFile.writeText(Json.encodeToString(data))
            cytoidID.setLastCacheTime<T>(currentTimeStamp)
            return@withContext true
        }
        */

    suspend fun loadBestRecords(cytoidID: String, timeStamp: Long): BestRecords =
        json.decodeFromString(load("BestRecords", cytoidID, timeStamp))

    suspend fun saveBestRecords(cytoidID: String, bestRecords: BestRecords) =
        save("BestRecords", cytoidID, bestRecords)

    suspend fun loadRecentRecords(cytoidID: String, timeStamp: Long): RecentRecords =
        json.decodeFromString(load("RecentRecords", cytoidID, timeStamp))

    suspend fun saveRecentRecords(cytoidID: String, recentRecords: RecentRecords) =
        save("RecentRecords", cytoidID, recentRecords)

    suspend fun loadProfileGraphQL(cytoidID: String, timeStamp: Long): ProfileGraphQL =
        json.decodeFromString(load("ProfileGraphQL", cytoidID, timeStamp))

    suspend fun saveProfileGraphQL(cytoidID: String, profileGraphQL: ProfileGraphQL) =
        save("ProfileGraphQL", cytoidID, profileGraphQL)

    suspend fun loadProfileDetails(cytoidID: String, timeStamp: Long): ProfileDetails =
        json.decodeFromString(load("ProfileDetails", cytoidID, timeStamp))

    suspend fun saveProfileDetails(cytoidID: String, profileDetails: ProfileDetails) =
        save("ProfileDetails", cytoidID, profileDetails)

    suspend fun loadProfileCommentList(cytoidID: String, timeStamp: Long): List<ProfileComment> =
        json.decodeFromString(load("ProfileCommentList", cytoidID, timeStamp))

    suspend fun saveProfileCommentList(cytoidID: String, profileCommentList: List<ProfileComment>) =
        save("ProfileCommentList", cytoidID, profileCommentList)

    private suspend fun load(type: String, cytoidID: String, timeStamp: Long): String =
        withContext(Dispatchers.IO) {
            val localTDir =
                BaseApplication.context.getExternalFilesDir(type)
            val targetUserTDir = File(localTDir, cytoidID)
            val targetUserTFile = File(targetUserTDir, "$timeStamp.json")
            targetUserTFile.readText()
        }

    private suspend inline fun <reified T> save(type: String, cytoidID: String, data: T) =
        withContext(Dispatchers.IO) {
            val localTDir =
                BaseApplication.context.getExternalFilesDir(type)
            val currentTimeStamp = System.currentTimeMillis()
            val targetUserTDir = File(localTDir, cytoidID)
            if (!targetUserTDir.exists()) targetUserTDir.mkdirs()
            val targetUserTFile =
                File(targetUserTDir, "${currentTimeStamp}.json")
            targetUserTFile.writeText(json.encodeToString(data))
            cytoidID.setLastCacheTime(type, currentTimeStamp)
        }
}