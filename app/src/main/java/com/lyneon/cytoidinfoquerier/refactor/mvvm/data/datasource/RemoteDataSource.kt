package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource

import android.util.Log
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileComments
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.refactor.mvvm.network.OkHttpSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object RemoteDataSource {
    suspend fun fetchBestRecords(cytoidID: String, count: Int) =
        fetch<BestRecords>(cytoidID = cytoidID, count = count)

    suspend fun fetchRecentRecords(cytoidID: String, count: Int) =
        fetch<RecentRecords>(cytoidID = cytoidID, count = count)

    suspend fun fetchProfileGraphQL(cytoidID: String) =
        fetch<ProfileGraphQL>(cytoidID = cytoidID)

    suspend fun fetchProfileComments(id: String) =
        fetch<ProfileComments>(id = id)

    suspend fun fetchProfileDetails(cytoidID: String) =
        fetch<ProfileDetails>(cytoidID = cytoidID)

    suspend inline fun <reified T> fetch(
        cytoidID: String = "",
        count: Int = 30,
        id: String = ""
    ): T {
        val okhttpClient = OkHttpSingleton.instance
        val requestBody =
            when (T::class) {
                BestRecords::class -> {
                    BestRecords.getRequestBodyString(cytoidID = cytoidID, bestRecordsLimit = count)
                }

                RecentRecords::class -> {
                    RecentRecords.getRequestBodyString(
                        cytoidID = cytoidID,
                        recentRecordsLimit = count
                    )
                }

                ProfileGraphQL::class ->
                    ProfileGraphQL.getRequestBodyString(cytoidID = cytoidID)

                ProfileComments::class -> null
                ProfileDetails::class -> null

                else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
            }
        val requestBuilder = Request.Builder().header("User-Agent", "CytoidClient/2.1.1")
        val request =
            if (requestBody == null)
                requestBuilder
                    .url(
                        when (T::class) {
                            ProfileComments::class -> "https://services.cytoid.io/threads/profile/$id"

                            ProfileDetails::class -> "https://services.cytoid.io/profile/$cytoidID/details"

                            else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
                        }
                    )
                    .build()
            else
                requestBuilder
                    .url("https://services.cytoid.io/graphql")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()
        val response = withContext(Dispatchers.IO) {
            okhttpClient.newCall(request).execute()
        }
        if (response.isSuccessful) {
            val responseBody = response.body!!.string()
            val data = try {
                Json.decodeFromString<T>(responseBody)
            } catch (e: Exception) {
                Log.e(
                    "RemoteDataSource",
                    "Error decoding ${T::class.simpleName} from response body: $responseBody"
                )
                throw e
            } finally {
                response.close()
            }
            return data
        } else {
            Log.e(
                "RemoteDataSource",
                "Error getting ${T::class.simpleName} from remote, status code: ${response.code}"
            )
            Log.i("RemoteDataSource", "Original request body: $requestBody")
            throw IllegalStateException("Error getting ${T::class.simpleName} from remote, status code: ${response.code}")
        }
    }
}