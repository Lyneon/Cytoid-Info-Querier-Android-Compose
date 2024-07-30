package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.FileInputStream
import java.io.FileOutputStream

object LocalDataSource {
    /*
    suspend inline fun <reified T> loadJSONString(cytoidID: String, timeStamp: Long): T =
        withContext(Dispatchers.IO) {
            val localTDir =
                BaseApplication.context.getExternalFilesDir(T::class.simpleName)
            val targetUserTDir = File(localTDir, cytoidID)
            val targetUserTFile = File(targetUserTDir, "$timeStamp.json")
            Json.decodeFromString(targetUserTFile.readText())
        }

    suspend inline fun <reified T> saveJSONString(cytoidID: String, data: T): Boolean =
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
        json.decodeFromString(loadJSONString("BestRecords", cytoidID, timeStamp))

    suspend fun saveBestRecords(cytoidID: String, bestRecords: BestRecords) =
        saveJSONString("BestRecords", cytoidID, bestRecords)

    suspend fun loadRecentRecords(cytoidID: String, timeStamp: Long): RecentRecords =
        json.decodeFromString(loadJSONString("RecentRecords", cytoidID, timeStamp))

    suspend fun saveRecentRecords(cytoidID: String, recentRecords: RecentRecords) =
        saveJSONString("RecentRecords", cytoidID, recentRecords)

    suspend fun loadProfileGraphQL(cytoidID: String, timeStamp: Long): ProfileGraphQL =
        json.decodeFromString(loadJSONString("ProfileGraphQL", cytoidID, timeStamp))

    suspend fun saveProfileGraphQL(cytoidID: String, profileGraphQL: ProfileGraphQL) =
        saveJSONString("ProfileGraphQL", cytoidID, profileGraphQL)

    suspend fun loadProfileDetails(cytoidID: String, timeStamp: Long): ProfileDetails =
        json.decodeFromString(loadJSONString("ProfileDetails", cytoidID, timeStamp))

    suspend fun saveProfileDetails(cytoidID: String, profileDetails: ProfileDetails) =
        saveJSONString("ProfileDetails", cytoidID, profileDetails)

    suspend fun loadProfileCommentList(cytoidID: String, timeStamp: Long): List<ProfileComment> =
        json.decodeFromString(loadJSONString("ProfileCommentList", cytoidID, timeStamp))

    suspend fun saveProfileCommentList(cytoidID: String, profileCommentList: List<ProfileComment>) =
        saveJSONString("ProfileCommentList", cytoidID, profileCommentList)

    suspend fun loadAvatarBitmap(cytoidID: String, size: AvatarSize): Bitmap =
        loadImageBitmap("avatar/$cytoidID", size.value)

    suspend fun saveAvatarBitmap(cytoidID: String, size: AvatarSize, bitmap: Bitmap) =
        saveImageBitmap("avatar/$cytoidID", size.value, bitmap)

    fun getAvatarBitmapFile(cytoidID: String, size: AvatarSize): File =
        File(BaseApplication.context.getExternalFilesDir("avatar/$cytoidID"), size.value)

    suspend fun loadBackgroundImageBitmap(levelUID: String, size: BackgroundImageSize) =
        loadImageBitmap("background_image/$levelUID", size.value)

    suspend fun saveBackgroundImageBitmap(
        levelUID: String,
        size: BackgroundImageSize,
        bitmap: Bitmap
    ) =
        saveImageBitmap("background_image/$levelUID", size.value, bitmap)

    fun getBackgroundImageBitmapFile(levelUID: String, size: BackgroundImageSize) =
        File(BaseApplication.context.getExternalFilesDir("background_image/$levelUID"), size.value)

    private suspend fun loadJSONString(type: String, cytoidID: String, timeStamp: Long): String =
        withContext(Dispatchers.IO) {
            val localTDir =
                BaseApplication.context.getExternalFilesDir(type)
            val targetUserTDir = File(localTDir, cytoidID)
            val targetUserTFile = File(targetUserTDir, "$timeStamp.json")
            targetUserTFile.readText()
        }

    private suspend inline fun <reified T> saveJSONString(type: String, cytoidID: String, data: T) =
        withContext(Dispatchers.IO) {
            val localTDir =
                BaseApplication.context.getExternalFilesDir(type)
            val currentTimeStamp = System.currentTimeMillis()
            val targetUserTDir = File(localTDir, cytoidID)
            val targetUserTFile =
                File(targetUserTDir, "${currentTimeStamp}.json")
            targetUserTFile.parentFile?.mkdirs()
            targetUserTFile.writeText(json.encodeToString(data))
            cytoidID.setLastCacheTime(type, currentTimeStamp)
        }

    private suspend fun loadImageBitmap(path: String, fileName: String): Bitmap =
        withContext(Dispatchers.IO) {
            val localImageDir =
                BaseApplication.context.getExternalFilesDir(path)
            val targetImageFile = File(localImageDir, fileName)
            FileInputStream(targetImageFile).use {
                BitmapFactory.decodeStream(it)
            }
        }

    private suspend fun saveImageBitmap(path: String, fileName: String, bitmap: Bitmap) =
        withContext(Dispatchers.IO) {
            val localImageDir =
                BaseApplication.context.getExternalFilesDir(path)
            val targetImageFile = File(localImageDir, fileName)
            targetImageFile.parentFile?.mkdirs()
            targetImageFile.createNewFile()
            FileOutputStream(targetImageFile).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }

    enum class AvatarSize(val value: String) {
        ORIGINAL("original"),
        LARGE("large")
    }

    enum class BackgroundImageSize(val value: String) {
        THUMBNAIL("thumbnail"),
        ORIGINAL("original")
    }
}