package com.lyneon.cytoidinfoquerier.data.datasource

import android.util.Log
import com.lyneon.cytoidinfoquerier.data.GraphQL
import com.lyneon.cytoidinfoquerier.data.constant.OkHttpSingleton
import com.lyneon.cytoidinfoquerier.data.constant.RecordQueryOrder
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelOrder
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelSortingStrategy
import com.lyneon.cytoidinfoquerier.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileComment
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.data.model.webapi.SearchLevelsResult
import com.lyneon.cytoidinfoquerier.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

object RemoteDataSource {
    suspend fun fetchBestRecords(cytoidID: String, count: Int): BestRecords {
        val requestBody = GraphQL.getQueryString(
            BestRecords.getRequestBodyString(
                cytoidID = cytoidID,
                bestRecordsLimit = count
            )
        )
        return fetch<BestRecords>(
            Request.Builder()
                .url("https://services.cytoid.io/graphql")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .cytoidHeader()
                .build()
        )
    }

    suspend fun fetchRecentRecords(
        cytoidID: String,
        count: Int,
        sort: RecordQuerySort,
        order: RecordQueryOrder
    ): RecentRecords {
        val requestBody = GraphQL.getQueryString(
            RecentRecords.getRequestBodyString(
                cytoidID = cytoidID,
                recentRecordsLimit = count,
                recentRecordsSort = sort,
                recentRecordsOrder = order
            )
        )
        return fetch<RecentRecords>(
            Request.Builder()
                .url("https://services.cytoid.io/graphql")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .cytoidHeader()
                .build()
        ).apply {
            this.queryArguments =
                RecentRecords.RecentRecordsQueryArguments(cytoidID, count, sort, order)
        }
    }

    suspend fun fetchProfileGraphQL(cytoidID: String): ProfileGraphQL {
        val requestBody =
            GraphQL.getQueryString(ProfileGraphQL.getRequestBodyString(cytoidID = cytoidID))
        return fetch<ProfileGraphQL>(
            Request.Builder()
                .url("https://services.cytoid.io/graphql")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .cytoidHeader()
                .build()
        )
    }

    suspend fun fetchProfileCommentList(id: String): List<ProfileComment> {
        return fetch<List<ProfileComment>>(
            Request.Builder()
                .url("https://services.cytoid.io/threads/profile/$id")
                .cytoidHeader()
                .build()
        )
    }

    suspend fun fetchProfileDetails(cytoidID: String): ProfileDetails {
        return fetch<ProfileDetails>(
            Request.Builder()
                .url("https://services.cytoid.io/profile/$cytoidID/details")
                .cytoidHeader()
                .build()
        )
    }

    suspend fun searchLevels(
        search: String,
        sortStrategy: SearchLevelSortingStrategy,
        order: SearchLevelOrder,
        page: Int,
        limit: Int,
        featured: Boolean,
        qualified: Boolean
    ): List<SearchLevelsResult> {
        return fetch<List<SearchLevelsResult>>(
            Request.Builder()
                .url("https://services.cytoid.io/search/levels?search=$search&sort=${sortStrategy.value}&order=${order.value}&page=$page&limit=$limit&featured=$featured&qualified=$qualified")
                .cytoidHeader()
                .build()
        )
    }

    private fun Request.Builder.cytoidHeader() = this.header("User-Agent", "CytoidClient/2.1.1")

    private suspend inline fun <reified T> fetch(request: Request): T {
        val response = withContext(Dispatchers.IO) {
            OkHttpSingleton.instance.newCall(request).execute()
        }
        if (response.isSuccessful) {
            response.use {
                val responseBody = response.body!!
                return responseBody.decode<T>()
            }
        } else {
            Log.e(
                "RemoteDataSource",
                "Error getting response from remote, status code: ${response.code}"
            )
            Log.i("RemoteDataSource", "Original request body: ${request.body}")
            throw IllegalStateException("Error getting response from remote, status code: ${response.code}")
        }
    }

    private inline fun <reified T> ResponseBody.decode(): T {
        val data = try {
            json.decodeFromString<T>(this.string())
        } catch (e: Exception) {
            Log.e(
                "RemoteDataSource",
                "Error decoding data from response body: $this"
            )
            throw e
        }
        return data
    }
}