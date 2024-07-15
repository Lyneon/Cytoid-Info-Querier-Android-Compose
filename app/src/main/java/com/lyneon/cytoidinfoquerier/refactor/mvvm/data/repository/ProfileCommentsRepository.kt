package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileComments
import com.lyneon.cytoidinfoquerier.util.extension.getLastCacheTime

class ProfileCommentsRepository {
    suspend fun getProfileComments(
        cytoidID: String,
        id: String,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchProfileComments(id)
    else {
        val lastProfileCommentsCacheTime = cytoidID.getLastCacheTime<ProfileComments>()
        if (System.currentTimeMillis() - lastProfileCommentsCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.load<ProfileComments>(
                cytoidID,
                lastProfileCommentsCacheTime
            )
        else
            RemoteDataSource.fetchProfileComments(id).also {
                LocalDataSource.save(cytoidID, it)
            }
    }
}