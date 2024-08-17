package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.util.getLastProfileDetailsCacheTime

class ProfileDetailsRepository {
    suspend fun getProfileDetails(
        cytoidID: String,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchProfileDetails(cytoidID).also {
            LocalDataSource.saveProfileDetails(cytoidID, it)
        }
    else {
        val lastProfileDetailsCacheTime = cytoidID.getLastProfileDetailsCacheTime()
        if (System.currentTimeMillis() - lastProfileDetailsCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.loadProfileDetails(cytoidID, lastProfileDetailsCacheTime)
        else
            RemoteDataSource.fetchProfileDetails(cytoidID).also {
                LocalDataSource.saveProfileDetails(cytoidID, it)
            }
    }
}