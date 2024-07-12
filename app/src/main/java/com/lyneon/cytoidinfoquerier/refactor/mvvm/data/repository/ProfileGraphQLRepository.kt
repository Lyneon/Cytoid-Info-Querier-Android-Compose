package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.util.extension.getLastCacheTime

class ProfileGraphQLRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getProfileGraphQL(
        cytoidID: String,
        disableLocalCache: Boolean = false
    ) = if (disableLocalCache)
        remoteDataSource.fetchProfileGraphQL(cytoidID)
    else {
        val lastProfileGraphQLCacheTime = cytoidID.getLastCacheTime<ProfileGraphQL>()
        if (System.currentTimeMillis() - lastProfileGraphQLCacheTime <= 1000 * 60 * 60 * 6)
            localDataSource.load<ProfileGraphQL>(
                cytoidID,
                lastProfileGraphQLCacheTime
            )
        else
            remoteDataSource.fetchProfileGraphQL(cytoidID).also {
                localDataSource.save(cytoidID, it)
            }
    }
}