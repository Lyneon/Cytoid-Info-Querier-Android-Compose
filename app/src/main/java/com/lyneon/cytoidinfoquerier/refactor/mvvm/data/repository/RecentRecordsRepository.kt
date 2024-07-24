package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.util.extension.getLastRecentRecordsCacheTime

class RecentRecordsRepository {
    suspend fun getRecentRecords(
        cytoidID: String,
        count: Int,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchRecentRecords(cytoidID, count)
    else {
        val lastRecentRecordsCacheTime = cytoidID.getLastRecentRecordsCacheTime()
        if (System.currentTimeMillis() - lastRecentRecordsCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.loadRecentRecords(cytoidID, lastRecentRecordsCacheTime)
        else
            RemoteDataSource.fetchRecentRecords(cytoidID, count).also {
                LocalDataSource.saveRecentRecords(cytoidID, it)
            }
    }
}