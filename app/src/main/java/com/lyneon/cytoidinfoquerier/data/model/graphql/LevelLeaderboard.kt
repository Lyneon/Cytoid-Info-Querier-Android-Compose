package com.lyneon.cytoidinfoquerier.data.model.graphql

import com.lyneon.cytoidinfoquerier.data.model.graphql.type.UserRecord
import kotlinx.serialization.Serializable

@Serializable
data class LevelLeaderboard(
    val data: LevelLeaderboardData
) {
    @Serializable
    data class LevelLeaderboardData(
        val chart: Chart?
    ) {
        @Serializable
        data class Chart(
            val id: Int,
            val type: String,
            val numPlayers: Int,
            val leaderboard: List<LeaderboardRecord>
        ) {
            @Serializable
            data class LeaderboardRecord(
                val id: Int,
                val date: String,
                val accuracy: Float,
                val score: Int,
                val mods: List<String>,
                val details: UserRecord.RecordDetails,
                val owner: User?
            ) {
                @Serializable
                data class User(
                    val id: String,
                    val uid: String?,
                    val avatar: Avatar
                ) {
                    @Serializable
                    data class Avatar(
                        val original: String?,
                        val large: String?,
                        val small: String?
                    )
                }
            }
        }
    }

    companion object {
        fun getRequestBodyString(levelUID: String,difficultyType: String, start: Int, limit: Int) =
            """query FetchLevelLeaderboard {
                chart(levelUid: "$levelUID", chartType: "$difficultyType") {
                    id
                    type
                    numPlayers
                    leaderboard(start: $start, limit: $limit) {
                        id
                        date
                        accuracy
                        mods
                        details {
                            perfect
                            great
                            good
                            bad
                            miss
                            maxCombo
                        }
                        score
                        owner {
                            id
                            uid
                            avatar {
                                original
                                large
                                small
                            }
                        }
                    }
                }
            }"""
    }
}