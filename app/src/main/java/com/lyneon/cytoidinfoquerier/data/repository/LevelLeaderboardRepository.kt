package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.data.model.graphql.LevelLeaderboard

class LevelLeaderboardRepository {
    suspend fun getLevelLeaderboard(
        levelUID: String,
        difficultyType: String,
        start: Int,
        limit: Int
    ): LevelLeaderboard {
        return RemoteDataSource.fetchLevelLeaderboard(levelUID, difficultyType, start, limit)
    }
}