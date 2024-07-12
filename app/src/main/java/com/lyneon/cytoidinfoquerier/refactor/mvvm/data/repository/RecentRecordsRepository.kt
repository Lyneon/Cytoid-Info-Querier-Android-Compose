package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.util.extension.getLastCacheTime

class RecentRecordsRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getRecentRecords(
        cytoidID: String,
        count: Int,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        remoteDataSource.fetchRecentRecords(cytoidID, count)
    else {
        val lastRecentRecordsCacheTime = cytoidID.getLastCacheTime<RecentRecords>()
        if (System.currentTimeMillis() - lastRecentRecordsCacheTime <= 1000 * 60 * 60 * 6)
            localDataSource.load<RecentRecords>(
                cytoidID,
                lastRecentRecordsCacheTime
            )
        else
            remoteDataSource.fetchRecentRecords(cytoidID, count).also {
                localDataSource.save(cytoidID, it)
            }
    }
}