package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.util.extension.getLastBestRecordsCacheTime

class BestRecordsRepository {
    suspend fun getBestRecords(
        cytoidID: String,
        count: Int,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchBestRecords(cytoidID, count)
    else {
        val lastBestRecordsCacheTime = cytoidID.getLastBestRecordsCacheTime()
        if (System.currentTimeMillis() - lastBestRecordsCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.loadBestRecords(cytoidID, lastBestRecordsCacheTime)
        else
            RemoteDataSource.fetchBestRecords(cytoidID, count).also {
                LocalDataSource.saveBestRecords(cytoidID, it)
            }
    }
}