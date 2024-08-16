package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.constant.RecordQueryOrder
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort
import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.util.extension.getLastRecentRecordsCacheTime

class RecentRecordsRepository {
    suspend fun getRecentRecords(
        cytoidID: String,
        count: Int,
        sort: RecordQuerySort,
        order: RecordQueryOrder,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchRecentRecords(cytoidID, count, sort, order).also {
            if (it.data.profile != null) LocalDataSource.saveRecentRecords(cytoidID, it)
        }
    else {
        val lastRecentRecordsCacheTime = cytoidID.getLastRecentRecordsCacheTime()
        if (System.currentTimeMillis() - lastRecentRecordsCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.loadRecentRecords(cytoidID, lastRecentRecordsCacheTime)
        else
            RemoteDataSource.fetchRecentRecords(cytoidID, count, sort, order).also {
                if (it.data.profile != null) LocalDataSource.saveRecentRecords(cytoidID, it)
            }
    }

    suspend fun getSpecificCacheRecentRecords(cytoidID: String, timeStamp: Long): RecentRecords {
        return LocalDataSource.loadRecentRecords(cytoidID, timeStamp)
    }
}