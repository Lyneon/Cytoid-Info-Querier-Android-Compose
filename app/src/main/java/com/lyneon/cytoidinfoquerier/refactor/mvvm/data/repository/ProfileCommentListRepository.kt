package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.util.extension.getLastProfileCommentListCacheTime

class ProfileCommentListRepository {
    suspend fun getProfileCommentList(
        cytoidID: String,
        id: String,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchProfileCommentList(id)
    else {
        val lastProfileCommentListCacheTime = cytoidID.getLastProfileCommentListCacheTime()
        if (System.currentTimeMillis() - lastProfileCommentListCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.loadProfileCommentList(cytoidID, lastProfileCommentListCacheTime)
        else
            RemoteDataSource.fetchProfileCommentList(id).also {
                LocalDataSource.saveProfileCommentList(cytoidID, it)
            }
    }
}