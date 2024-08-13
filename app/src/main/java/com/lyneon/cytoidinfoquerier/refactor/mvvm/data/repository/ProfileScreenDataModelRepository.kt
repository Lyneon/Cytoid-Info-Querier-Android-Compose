package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.screen.ProfileScreenDataModel
import com.lyneon.cytoidinfoquerier.util.extension.getLastProfileScreenDataModelCacheTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ProfileScreenDataModelRepository {
    private fun getFetchRemoteAndSaveToLocalJob(cytoidID: String) = CoroutineScope(Dispatchers.IO).async {
        val profileGraphQLJob = async { RemoteDataSource.fetchProfileGraphQL(cytoidID) }
        val profileDetailsJob = async { RemoteDataSource.fetchProfileDetails(cytoidID) }
        val commentListJob =
            async {
                profileGraphQLJob.await().data.profile?.user?.let {
                    RemoteDataSource.fetchProfileCommentList(it.id)
                } ?: emptyList()
            }
        ProfileScreenDataModel(
            profileDetailsJob.await().also { LocalDataSource.saveProfileDetails(cytoidID, it) },
            profileGraphQLJob.await().also { LocalDataSource.saveProfileGraphQL(cytoidID, it) },
            commentListJob.await().also { LocalDataSource.saveProfileCommentList(cytoidID, it) }
        ).also { LocalDataSource.saveProfileScreenDataModel(cytoidID, it) }
    }

    suspend fun getProfileScreenDataModel(
        cytoidID: String,
        disableLocalCache: Boolean
    ) = if (disableLocalCache) {
        getFetchRemoteAndSaveToLocalJob(cytoidID).await()
    } else {
        val lastProfileScreenDataModelCacheTime = cytoidID.getLastProfileScreenDataModelCacheTime()
        if (System.currentTimeMillis() - lastProfileScreenDataModelCacheTime <= 1000 * 60 * 60 * 6)
            LocalDataSource.loadProfileScreenDataModel(
                cytoidID,
                lastProfileScreenDataModelCacheTime
            )
        else getFetchRemoteAndSaveToLocalJob(cytoidID).await()
    }
}