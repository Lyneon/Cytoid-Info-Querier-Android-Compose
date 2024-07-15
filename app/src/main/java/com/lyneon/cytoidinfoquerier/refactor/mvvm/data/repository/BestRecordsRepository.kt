package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.util.extension.getLastCacheTime

class BestRecordsRepository {
    suspend fun getBestRecords(
        cytoidID: String,
        count: Int,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchBestRecords(cytoidID, count)
    else {
        val lastBestRecordsCacheTime = cytoidID.getLastCacheTime<BestRecords>()
        if (System.currentTimeMillis() - lastBestRecordsCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.load<BestRecords>(
                cytoidID,
                lastBestRecordsCacheTime
            )
        else
            RemoteDataSource.fetchBestRecords(cytoidID, count).also {
                LocalDataSource.save(cytoidID, it)
            }
    }
}