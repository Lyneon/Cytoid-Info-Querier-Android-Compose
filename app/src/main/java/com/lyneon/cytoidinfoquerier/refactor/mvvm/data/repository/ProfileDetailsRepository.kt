package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.util.extension.getLastCacheTime

class ProfileDetailsRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getProfileDetails(
        cytoidID: String,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        remoteDataSource.fetchProfileDetails(cytoidID)
    else {
        val lastProfileDetailsCacheTime = cytoidID.getLastCacheTime<ProfileDetails>()
        if (System.currentTimeMillis() - lastProfileDetailsCacheTime <= 1000 * 60 * 60 * 6)
            localDataSource.load<ProfileDetails>(
                cytoidID,
                lastProfileDetailsCacheTime
            )
        else
            remoteDataSource.fetchProfileDetails(cytoidID).also {
                localDataSource.save(cytoidID, it)
            }
    }
}