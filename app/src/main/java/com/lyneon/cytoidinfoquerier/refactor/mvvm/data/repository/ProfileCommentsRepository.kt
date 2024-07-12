package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileComments
import com.lyneon.cytoidinfoquerier.util.extension.getLastCacheTime

class ProfileCommentsRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getProfileComments(
        cytoidID: String,
        id: String,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        remoteDataSource.fetchProfileComments(id)
    else {
        val lastProfileCommentsCacheTime = cytoidID.getLastCacheTime<ProfileComments>()
        if (System.currentTimeMillis() - lastProfileCommentsCacheTime <= 1000 * 60 * 60 * 6)
            localDataSource.load<ProfileComments>(
                cytoidID,
                lastProfileCommentsCacheTime
            )
        else
            remoteDataSource.fetchProfileComments(id).also {
                localDataSource.save(cytoidID, it)
            }
    }
}