package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.util.extension.getLastBestRecordsCacheTime
import com.lyneon.cytoidinfoquerier.util.extension.showToast

class BestRecordsRepository {
    suspend fun getBestRecords(
        cytoidID: String,
        count: Int,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchBestRecords(cytoidID, count)
    else {
        val lastBestRecordsCacheTime = cytoidID.getLastBestRecordsCacheTime()
        if (System.currentTimeMillis() - lastBestRecordsCacheTime <= 1000 * 60 * 60 * 6) {
            "正在从本地缓存加载数据".showToast()
            LocalDataSource.loadBestRecords(cytoidID, lastBestRecordsCacheTime)
        } else
            RemoteDataSource.fetchBestRecords(cytoidID, count).also {
                LocalDataSource.saveBestRecords(cytoidID, it)
            }
    }

    suspend fun getSpecificCacheBestRecords(cytoidID: String, timeStamp: Long): BestRecords {
        return LocalDataSource.loadBestRecords(cytoidID, timeStamp)
    }
}