package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.data.constant.OkHttpSingleton
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelOrder
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelSortingStrategy
import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.data.model.webapi.SearchLevelsResult
import com.lyneon.cytoidinfoquerier.json
import okhttp3.Request

class SearchLevelsRepository {
    suspend fun searchLevels(
        search: String,
        sortStrategy: SearchLevelSortingStrategy = SearchLevelSortingStrategy.CreationDate,
        order: SearchLevelOrder = SearchLevelOrder.Descending,
        page: Int,
        limit: Int,
        featured: Boolean,
        qualified: Boolean
    ): List<SearchLevelsResult> {
        if (search.isEmpty() || page < 0 || limit < 1) return emptyList()
        return RemoteDataSource.searchLevels(
            search,
            sortStrategy,
            order,
            page,
            limit,
            featured,
            qualified
        )
    }

    fun searchLevelsWithPagesCount(
        search: String,
        sortStrategy: SearchLevelSortingStrategy = SearchLevelSortingStrategy.CreationDate,
        order: SearchLevelOrder = SearchLevelOrder.Descending,
        page: Int,
        limit: Int,
        featured: Boolean,
        qualified: Boolean
    ): Pair<List<SearchLevelsResult>, Int> {
        val request = Request.Builder()
            .url("${CytoidConstant.serverUrl}/search/levels?search=$search&sort=${sortStrategy.value}&order=${order.value}&page=$page&limit=$limit&featured=$featured&qualified=$qualified")
            .removeHeader("User-Agent")
            .addHeader("User-Agent", CytoidConstant.clientUA)
            .build()
        val response = OkHttpSingleton.instance.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.let {
                val levels: List<SearchLevelsResult> = json.decodeFromString(it.string())
                val totalPages = response.headers["x-total-page"]?.toInt() ?: 1
                return Pair(levels, totalPages)
            } ?: throw Exception("Failed to parse levels")
        } else {
            throw Exception("Failed to fetch levels")
        }
    }
}