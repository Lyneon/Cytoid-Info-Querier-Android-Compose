package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.util.extension.getLastProfileGraphQLCacheTime

class ProfileGraphQLRepository {
    suspend fun getProfileGraphQL(
        cytoidID: String,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        RemoteDataSource.fetchProfileGraphQL(cytoidID).also {
            LocalDataSource.saveProfileGraphQL(cytoidID, it)
        }
    else {
        val lastProfileGraphQLCacheTime = cytoidID.getLastProfileGraphQLCacheTime()
        if (System.currentTimeMillis() - lastProfileGraphQLCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.loadProfileGraphQL(cytoidID, lastProfileGraphQLCacheTime)
        else
            RemoteDataSource.fetchProfileGraphQL(cytoidID).also {
                LocalDataSource.saveProfileGraphQL(cytoidID, it)
            }
    }
}