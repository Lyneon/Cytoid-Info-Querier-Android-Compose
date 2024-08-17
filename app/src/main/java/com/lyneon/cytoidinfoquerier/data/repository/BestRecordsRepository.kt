package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.util.getLastBestRecordsCacheTime

class BestRecordsRepository {
    suspend fun getBestRecords(
        cytoidID: String,
        count: Int,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchBestRecords(cytoidID, count).also {
            if (it.data.profile != null) LocalDataSource.saveBestRecords(cytoidID, it)
        }
    else {
        val lastBestRecordsCacheTime = cytoidID.getLastBestRecordsCacheTime()
        if (System.currentTimeMillis() - lastBestRecordsCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.loadBestRecords(cytoidID, lastBestRecordsCacheTime)
        else
            RemoteDataSource.fetchBestRecords(cytoidID, count).also {
                if (it.data.profile != null) LocalDataSource.saveBestRecords(cytoidID, it)
            }
    }

    suspend fun getSpecificCacheBestRecords(cytoidID: String, timeStamp: Long): BestRecords {
        return LocalDataSource.loadBestRecords(cytoidID, timeStamp)
    }
}