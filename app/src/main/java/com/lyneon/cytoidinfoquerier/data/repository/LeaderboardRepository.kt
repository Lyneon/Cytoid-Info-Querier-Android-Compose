package com.lyneon.cytoidinfoquerier.data.repository

import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.data.model.webapi.LeaderboardEntry

class LeaderboardRepository {
    suspend fun getLeaderboardTop(limit: Int): List<LeaderboardEntry> {
        return RemoteDataSource.fetchLeaderboard(limit)
    }

    suspend fun getLeaderboardAroundUser(limit: Int, userId: String): List<LeaderboardEntry> {
        return RemoteDataSource.fetchLeaderboard(limit, userId)
    }
}