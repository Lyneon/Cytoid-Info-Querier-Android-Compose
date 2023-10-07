package com.lyneon.cytoidinfoquerier.logic.model

import kotlinx.serialization.Serializable

@Serializable
data class Chart(
    val id: Int,
    val difficulty: Int,
    val type: String,
    val notesCount: Int,
    val numPlayers: Int,
    val leaderboard: ArrayList<LeaderboardRecord>
) {
    @Serializable
    data class LeaderboardRecord(
        val id: Int,
        val date: String,
        val score: Int,
        val accuracy: Float,
        val owner: User
    )

    @Serializable
    data class User(
        val id: String,
        val uid: String
    )

    fun getGQLQuery(
        levelUid: String,
        chartType: String,
        leaderboardStart: Int,
        leaderboardLimit: Int
    ): String = """{
  "operationName": null,
  "variables": {},
  "query": "{\n  chart(levelUid: \"$levelUid\", chartType: \"$chartType\") {\n    id\n    name\n    difficulty\n    type\n    notesCount\n    numPlayers\n    leaderboard(start: $leaderboardStart, limit: $leaderboardLimit) {\n      id\n      date\n      score\n      accuracy\n      owner {\n        id\n        uid\n      }\n    }\n  }\n}\n"
}"""
}