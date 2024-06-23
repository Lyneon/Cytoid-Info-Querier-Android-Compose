package com.lyneon.cytoidinfoquerier.logic

import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.data.GraphQL
import com.lyneon.cytoidinfoquerier.data.constant.RecordQueryOrder
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort
import com.lyneon.cytoidinfoquerier.data.model.graphql.Analytics
import com.lyneon.cytoidinfoquerier.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.data.model.ui.AnalyticsScreenDataModel
import com.lyneon.cytoidinfoquerier.data.model.ui.ProfileScreenIntegratedDataModel
import com.lyneon.cytoidinfoquerier.data.model.webapi.Comment
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileWebapi
import com.lyneon.cytoidinfoquerier.json
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.util.extension.lastQueryAnalyticsTime
import com.lyneon.cytoidinfoquerier.util.extension.lastQueryProfileTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import java.io.File

object Repository {
    suspend fun getProfileScreenIntegratedDataModel(
        cytoidID: String,
        useCache: Boolean
    ): ProfileScreenIntegratedDataModel = if (useCache) {
        // 从本地缓存中获取数据
        val profileCacheFile = File(
            BaseApplication.context.externalCacheDir,
            "profile/${cytoidID}/${cytoidID.lastQueryProfileTime}"
        )
        json.decodeFromString(profileCacheFile.readText())
    } else {
        // 从网络服务器获取数据并缓存到本地
        withContext(Dispatchers.IO) {
            val profiles = awaitAll(
                async { ProfileGraphQL.get(cytoidID) },
                async { ProfileWebapi.get(cytoidID) }
            )
            val profileGraphQL =
                profiles[0] as ProfileGraphQL
            val profileWebapi =
                profiles[1] as ProfileWebapi
            val comments =
                async { Comment.get(profileGraphQL.data.profile.user.id) }.await()
            ProfileScreenIntegratedDataModel(profileGraphQL, profileWebapi, comments).apply {
                val profileCacheFile = File(
                    BaseApplication.context.externalCacheDir,
                    "profile/${cytoidID}/${System.currentTimeMillis()}"
                )
                profileCacheFile.parentFile?.mkdirs()
                profileCacheFile.writeText(json.encodeToString(this))
            }
        }
    }

    suspend fun getAnalyticsScreenDataModel(
        cytoidID: String,
        ignoreCache: Boolean,
        bestRecordsCount: Int,
        recentRecordsCount: Int
    ): AnalyticsScreenDataModel =
        if (shouldLoadFromCache(cytoidID.lastQueryAnalyticsTime, ignoreCache)) {
            // 从本地缓存中获取数据
            val analyticsCacheFile = File(
                BaseApplication.context.externalCacheDir,
                "analytics/${cytoidID}/${cytoidID.lastQueryAnalyticsTime}"
            )
            json.decodeFromString(analyticsCacheFile.readText())
        } else {
            // 从网络服务器获取数据并缓存到本地
            withContext(Dispatchers.IO) {
                val analytics = getAnalytics(
                    cytoidID,
                    bestRecordsCount,
                    recentRecordsCount,
                    RecordQuerySort.Date,
                    RecordQueryOrder.DESC
                )
                AnalyticsScreenDataModel(analytics).apply {
                    val analyticsCacheFile = File(
                        BaseApplication.context.externalCacheDir,
                        "analytics/${cytoidID}/${System.currentTimeMillis()}"
                    )
                    analyticsCacheFile.parentFile?.mkdirs()
                    analyticsCacheFile.writeText(json.encodeToString(this))
                }
            }
        }


    suspend fun getProfileGraphQL(cytoidID: String): ProfileGraphQL = withContext(Dispatchers.IO) {
        async {
            val response = NetRequest.getGQLResponseJSONString(
                GraphQL.getQueryString(
                    ProfileGraphQL.getQueryString(cytoidID)
                )
            )
            if (response == null) throw Exception("Response is null")
            json.decodeFromString<ProfileGraphQL>(response)
        }.await()
    }


    suspend fun getAnalytics(
        cytoidID: String,
        bestRecordsCount: Int,
        recentRecordsCount: Int,
        recentRecordsSort: RecordQuerySort,
        recentRecordsOrder: RecordQueryOrder
    ): Analytics = withContext(Dispatchers.IO) {
        async {
            val response = NetRequest.getGQLResponseJSONString(
                GraphQL.getQueryString(
                    Analytics.getQueryString(
                        cytoidID,
                        recentRecordsCount,
                        recentRecordsSort.name,
                        recentRecordsOrder.name,
                        bestRecordsCount
                    )
                )
            )
            if (response == null) throw Exception("Response is null")
            Analytics.decodeFromJSONString(response)
        }.await()
    }

    suspend fun getCommentsList(cytoidID: String): List<Comment> = withContext(Dispatchers.IO) {
        async {
            Comment.get(cytoidID)
        }.await()
    }

    private fun shouldLoadFromCache(lastCacheTime: Long, disableCache: Boolean): Boolean =
        !disableCache && System.currentTimeMillis() - lastCacheTime < 1000 * 60 * 60 * 6

    private fun shouldLoadFromNetwork(lastCacheTime: Long, disableCache: Boolean): Boolean =
        !shouldLoadFromCache(lastCacheTime, disableCache)
}