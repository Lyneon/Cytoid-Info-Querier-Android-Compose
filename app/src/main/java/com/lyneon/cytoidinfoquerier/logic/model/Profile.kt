package com.lyneon.cytoidinfoquerier.logic.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileData(
    val profile: Profile
)

@Serializable
data class Profile(
    val id: String,
    val user: User,
    val rating: Float,
    val exp: ProfileExp,
    val grades: ProfileGrades,
    val activity: ProfileActivity,
    val timeseries: ArrayList<ProfileTimeSeries>,
    val badges: ArrayList<Badge>,
    val recentRecords: ArrayList<UserRecord>,
    val bestRecords: ArrayList<UserRecord>
) {
    @Serializable
    data class User(
        val id: String,
        val uid: String
    )

    @Serializable
    data class ProfileExp(
        val basicExp: Int,
        val levelExp: Int,
        val totalExp: Int,
        val currentLevel: Int,
        val nextLevelExp: Int,
        val currentLevelExp: Int
    )

    @Serializable
    data class ProfileGrades(
        val MAX: Int,
        val SS: Int,
        val S: Int,
        val A: Int,
        val B: Int,
        val C: Int,
        val D: Int,
        val F: Int
    )

    @Serializable
    data class ProfileActivity(
        val totalRankedPlays: Long,
        val clearedNotes: Long,
        val maxCombo: Long,
        val averageRankedAccuracy: Float,
        val totalRankedScore: Long,
        val totalPlayTime: Float
    )

    @Serializable
    data class ProfileTimeSeries(
        val cumulativeRating: Float,
        val cumulativeAccuracy: Float,
        val week: Int,
        val year: Int,
        val accuracy: Float,
        val rating: Float,
        val count: Int
    )

    @Serializable
    data class Badge(
        val uid: String,
        val title: String,
        val description: String,
        val metadata: BadgeMetadata
    ) {
        @Serializable
        data class BadgeMetadata(
            val imageUrl: String? = null
        )
    }

    @Serializable
    data class UserRecord(
        val id: Int,
        val score: Int,
        val accuracy: Float,
        val mods: ArrayList<String>,
        val ranked: Boolean,
        val details: RecordDetails,
        val rating: Float,
        val date: String,
        val chart: RecordChart
    ) {
        @Serializable
        data class RecordDetails(
            val perfect: Int,
            val great: Int,
            val good: Int,
            val bad: Int,
            val miss: Int,
            val maxCombo: Int
        )

        @Serializable
        data class RecordChart(
            val id: Int,
            val difficulty: Int,
            val type: String,
            val notesCount: Int,
            val level: RecordLevel
        ) {
            @Serializable
            data class RecordLevel(
                val id: Int,
                val uid: String,
                val title: String,
                val bundle: Bundle
            ) {
                @Serializable
                data class Bundle(
                    val backgroundImage: BackgroundImage
                ) {
                    @Serializable
                    data class BackgroundImage(
                        val thumbnail: String,
                        val original: String
                    )
                }
            }
        }
    }

    companion object {
        fun getGQLQueryString(
            uid: String,
            recentRecordsLimit: Int = 0,
            recentRecordsSort: String = RecordQuerySort.Date,
            recentRecordsOrder: String = QueryOrder.DESC,
            bestRecordsLimit: Int = 0
        ): String = """{
  "operationName": null,
  "variables": {},
  "query": "{\n  profile(uid: \"$uid\") {\n    id\n    user {\n      id\n      uid\n    }\n    rating\n    exp {\n      basicExp\n      levelExp\n      totalExp\n      currentLevel\n      nextLevelExp\n      currentLevelExp\n    }\n    grades {\n      MAX\n      SS\n      S\n      A\n      B\n      C\n      D\n      F\n    }\n    activity {\n      totalRankedPlays\n      clearedNotes\n      maxCombo\n      averageRankedAccuracy\n      totalRankedScore\n      totalPlayTime\n    }\n    timeseries {\n      cumulativeRating\n      cumulativeAccuracy\n      week\n      year\n      accuracy\n      rating\n      count\n    }\n    badges {\n      uid\n      title\n      description\n      metadata\n    }\n    recentRecords(limit: $recentRecordsLimit, sort: $recentRecordsSort, order: $recentRecordsOrder) {\n      ...UserRecord\n    }\n    bestRecords(limit: $bestRecordsLimit) {\n      ...UserRecord\n    }\n  }\n}\n\nfragment UserRecord on UserRecord {\n  id\n  score\n  accuracy\n  mods\n  ranked\n  details {\n    perfect\n    great\n    good\n    bad\n    miss\n    maxCombo\n  }\n  rating\n  date\n  chart {\n    id\n    difficulty\n    type\n    notesCount\n    level {\n      id\n      uid\n      title\n      bundle {\n        backgroundImage {\n          thumbnail\n          original\n        }\n      }\n    }\n  }\n}\n"
}"""
    }
}