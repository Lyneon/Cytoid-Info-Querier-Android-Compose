package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.RecordQueryOrder
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort
import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.lyneon.cytoidinfoquerier.util.getLastRecentRecordsCacheTime

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
        val passedTime = System.currentTimeMillis() - lastRecentRecordsCacheTime
        if (passedTime <= 1000 * 60 * 60 * 6) {
            BaseApplication.context.getString(
                R.string.loading_local_cache,
                (passedTime / 1000 / 60).toString()
            ).showToast()
            LocalDataSource.loadRecentRecords(cytoidID, lastRecentRecordsCacheTime)
        } else
            RemoteDataSource.fetchRecentRecords(cytoidID, count, sort, order).also {
                if (it.data.profile != null) LocalDataSource.saveRecentRecords(cytoidID, it)
            }
    }

    suspend fun getSpecificCacheRecentRecords(cytoidID: String, timeStamp: Long): RecentRecords {
        return LocalDataSource.loadRecentRecords(cytoidID, timeStamp)
    }
}