package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelOrder
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelSortingStrategy
import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.data.model.webapi.SearchLevelsResult

class SearchLevelsRepository {
    suspend fun searchLevels(
        search: String,
        sortStrategy: SearchLevelSortingStrategy = SearchLevelSortingStrategy.CreationDate,
        order: SearchLevelOrder = SearchLevelOrder.Descending,
        page: Int,
        limit: Int
    ): List<SearchLevelsResult> {
        if (search.isEmpty() || page < 0 || limit < 1) return emptyList()
        return RemoteDataSource.searchLevels(search, sortStrategy, order, page, limit)
    }
}