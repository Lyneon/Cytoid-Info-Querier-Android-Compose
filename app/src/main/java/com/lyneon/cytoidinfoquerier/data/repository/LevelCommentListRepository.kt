package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.data.model.webapi.LevelComment

class LevelCommentListRepository {
    suspend fun getLevelCommentList(levelUID: String): List<LevelComment> {
        return RemoteDataSource.fetchLevelComments(levelUID)
    }
}