package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.util.getLastProfileCommentListCacheTime

class ProfileCommentListRepository {
    suspend fun getProfileCommentList(
        cytoidID: String,
        id: String?,
        disableLocalCache: Boolean = false
    ) = if (id == null) emptyList() else if (disableLocalCache)
        RemoteDataSource.fetchProfileCommentList(id).also {
            LocalDataSource.saveProfileCommentList(cytoidID, it)
        }
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