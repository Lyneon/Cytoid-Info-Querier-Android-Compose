package com.lyneon.cytoidinfoquerier.data.datasource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.data.enums.AvatarSize
import com.lyneon.cytoidinfoquerier.data.enums.ImageSize
import com.lyneon.cytoidinfoquerier.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.data.model.local.AnalyticsPreset
import com.lyneon.cytoidinfoquerier.data.model.screen.ProfileScreenDataModel
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileComment
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.json
import com.lyneon.cytoidinfoquerier.util.setLastCacheTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object LocalDataSource {
    suspend fun loadBestRecords(cytoidID: String, timeStamp: Long): BestRecords =
        json.decodeFromString(loadJSONString(LocalDataType.BestRecords.name, cytoidID, timeStamp))

    suspend fun saveBestRecords(cytoidID: String, bestRecords: BestRecords) =
        saveJSONString(LocalDataType.BestRecords.name, cytoidID, bestRecords)

    suspend fun loadRecentRecords(cytoidID: String, timeStamp: Long): RecentRecords =
        json.decodeFromString(loadJSONString(LocalDataType.RecentRecords.name, cytoidID, timeStamp))

    suspend fun saveRecentRecords(cytoidID: String, recentRecords: RecentRecords) =
        saveJSONString(LocalDataType.RecentRecords.name, cytoidID, recentRecords)

    suspend fun loadProfileGraphQL(cytoidID: String, timeStamp: Long): ProfileGraphQL =
        json.decodeFromString(
            loadJSONString(
                LocalDataType.ProfileGraphQL.name,
                cytoidID,
                timeStamp
            )
        )

    suspend fun saveProfileGraphQL(cytoidID: String, profileGraphQL: ProfileGraphQL) =
        saveJSONString(LocalDataType.ProfileGraphQL.name, cytoidID, profileGraphQL)

    suspend fun loadProfileDetails(cytoidID: String, timeStamp: Long): ProfileDetails =
        json.decodeFromString(
            loadJSONString(
                LocalDataType.ProfileDetails.name,
                cytoidID,
                timeStamp
            )
        )

    suspend fun saveProfileDetails(cytoidID: String, profileDetails: ProfileDetails) =
        saveJSONString(LocalDataType.ProfileDetails.name, cytoidID, profileDetails)

    suspend fun loadProfileCommentList(cytoidID: String, timeStamp: Long): List<ProfileComment> =
        json.decodeFromString(
            loadJSONString(
                LocalDataType.ProfileCommentList.name,
                cytoidID,
                timeStamp
            )
        )

    suspend fun saveProfileCommentList(cytoidID: String, profileCommentList: List<ProfileComment>) =
        saveJSONString(LocalDataType.ProfileCommentList.name, cytoidID, profileCommentList)

    suspend fun loadProfileScreenDataModel(
        cytoidID: String,
        timeStamp: Long
    ): ProfileScreenDataModel =
        json.decodeFromString(
            loadJSONString(
                LocalDataType.ProfileScreenDataModel.name,
                cytoidID,
                timeStamp
            )
        )

    suspend fun saveProfileScreenDataModel(
        cytoidID: String,
        profileScreenDataModel: ProfileScreenDataModel
    ) =
        saveJSONString(LocalDataType.ProfileScreenDataModel.name, cytoidID, profileScreenDataModel)

    suspend fun loadAvatarBitmap(cytoidID: String, size: AvatarSize): Bitmap =
        loadImageBitmap("${LocalDataType.Avatar.name}/$cytoidID", size.value)

    suspend fun saveAvatarBitmap(cytoidID: String, size: AvatarSize, bitmap: Bitmap) =
        saveImageBitmap("${LocalDataType.Avatar.name}/$cytoidID", size.value, bitmap)

    fun getAvatarBitmapFile(cytoidID: String, size: AvatarSize): File =
        File(
            BaseApplication.context.getExternalFilesDir("${LocalDataType.Avatar.name}/$cytoidID"),
            size.value
        )

    suspend fun loadBackgroundImageBitmap(levelUID: String, size: ImageSize) =
        loadImageBitmap("${LocalDataType.BackgroundImage.name}/$levelUID", size.value)

    suspend fun saveBackgroundImageBitmap(
        levelUID: String,
        size: ImageSize,
        bitmap: Bitmap
    ) =
        saveImageBitmap("${LocalDataType.BackgroundImage.name}/$levelUID", size.value, bitmap)

    fun getBackgroundImageBitmapFile(levelUID: String, size: ImageSize) =
        File(
            BaseApplication.context.getExternalFilesDir("${LocalDataType.BackgroundImage.name}/$levelUID"),
            size.value
        )

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
            val targetImageFile = File(localImageDir, "${fileName}.png")
            FileInputStream(targetImageFile).use {
                BitmapFactory.decodeStream(it)
            }
        }

    private suspend fun saveImageBitmap(path: String, fileName: String, bitmap: Bitmap) =
        withContext(Dispatchers.IO) {
            val localImageDir =
                BaseApplication.context.getExternalFilesDir(path)
            val targetImageFile = File(localImageDir, "${fileName}.png")
            targetImageFile.parentFile?.mkdirs()
            targetImageFile.createNewFile()
            FileOutputStream(targetImageFile).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }

    fun clearAvatar() = clearLocalData(LocalDataType.Avatar)
    fun clearBackgroundImage() = clearLocalData(LocalDataType.BackgroundImage)
    fun clearBestRecords() = clearLocalData(LocalDataType.BestRecords)
    fun clearRecentRecords() = clearLocalData(LocalDataType.RecentRecords)
    fun clearProfileGraphQL() = clearLocalData(LocalDataType.ProfileGraphQL)
    fun clearProfileDetails() = clearLocalData(LocalDataType.ProfileDetails)
    fun clearProfileCommentList() = clearLocalData(LocalDataType.ProfileCommentList)
    fun clearProfileScreenDataModel() = clearLocalData(LocalDataType.ProfileScreenDataModel)
    fun clearCollectionCoverImage() = clearLocalData(LocalDataType.CollectionCoverImage)
    fun clearAllLocalData() = LocalDataType.entries.forEach { clearLocalData(it) }

    fun clearLocalData(vararg types: LocalDataType) = CoroutineScope(Dispatchers.IO).launch {
        types.forEach { type ->
            BaseApplication.context.getExternalFilesDir(type.name)?.deleteRecursively()
        }
    }

    fun getCollectionCoverImageFile(collectionID: String, collectionCoverImageSize: ImageSize) =
        File(
            BaseApplication.context.getExternalFilesDir("${LocalDataType.CollectionCoverImage.name}/$collectionID"),
            collectionCoverImageSize.value
        )

    suspend fun saveCollectionCoverImage(
        collectionID: String,
        collectionCoverImageSize: ImageSize,
        bitmap: Bitmap
    ) = saveImageBitmap(
        "${LocalDataType.CollectionCoverImage.name}/$collectionID",
        collectionCoverImageSize.value,
        bitmap
    )

    enum class LocalDataType {
        Avatar,
        BackgroundImage,
        BestRecords,
        RecentRecords,
        ProfileGraphQL,
        ProfileDetails,
        ProfileCommentList,
        ProfileScreenDataModel,
        CollectionCoverImage,
        AnalyticsPresets;

        fun clearLocalCache() = clearLocalData(this)
    }

    suspend fun saveAnalyticsPreset(preset: AnalyticsPreset) {
        val jsonString = json.encodeToString(preset)
        withContext(Dispatchers.IO) {
            val localDir =
                BaseApplication.context.getExternalFilesDir(LocalDataType.AnalyticsPresets.name)
            val targetFile = File(
                localDir,
                "${URLEncoder.encode(preset.name, StandardCharsets.UTF_8.toString())}.json"
            )
            localDir?.mkdirs()
            targetFile.writeText(jsonString)
        }
    }

    suspend fun getAllAnalyticsPresets(): List<AnalyticsPreset> = withContext(Dispatchers.IO) {
        val localDir =
            BaseApplication.context.getExternalFilesDir(LocalDataType.AnalyticsPresets.name)
        localDir?.listFiles()?.mapNotNull {
            try {
                json.decodeFromString<AnalyticsPreset>(it.readText())
            } catch (e: Exception) {
                Log.e("AnalyticsPreset", e.message ?: "")
                null
            }
        } ?: emptyList()
    }

    suspend fun deleteAnalyticsPreset(presetName: String) {
        withContext(Dispatchers.IO) {
            val localDir =
                BaseApplication.context.getExternalFilesDir(LocalDataType.AnalyticsPresets.name)
            val targetFile = File(
                localDir,
                "${URLEncoder.encode(presetName, StandardCharsets.UTF_8.toString())}.json"
            )
            targetFile.delete()
        }
    }
}