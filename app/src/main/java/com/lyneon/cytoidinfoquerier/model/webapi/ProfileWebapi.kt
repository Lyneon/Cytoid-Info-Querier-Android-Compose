package com.lyneon.cytoidinfoquerier.model.webapi

import com.lyneon.cytoidinfoquerier.json
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class ProfileWebapi(
    val user: User,
    val badges: ArrayList<Badge>,
    val grade: Grade,
    val activities: Activities,
    val exp: Exp,
    val rating: Double,
    val timeSeries: ArrayList<TimeSeriesItem>,
    val lastActive: String?,
    val tier: Tier?,
    val character: Character
) {
    @Serializable
    data class User(
        val uid: String,
        val avatar: Avatar
    ) {
        @Serializable
        data class Avatar(
            val original: String,
            val large: String
        )
    }

    @Serializable
    data class Badge(
        val title: String,
        val description: String,
        val listed: Boolean,
        val date: String
    )

    @Serializable
    data class Grade(
        val MAX: Int? = 0,
        val SSS: Int? = 0,
        val SS: Int? = 0,
        val S: Int? = 0,
        val AA: Int? = 0,
        val A: Int? = 0,
        val B: Int? = 0,
        val C: Int? = 0,
        val D: Int? = 0,
        val F: Int? = 0
    )

    @Serializable
    data class Activities(
        val totalRankedPlays: Int,
        val clearedNotes: Long,
        val maxCombo: Int,
        val averageRankedAccuracy: Double,
        val totalRankedScore: Long,
        val totalPlayTime: Float
    )

    @Serializable
    data class Exp(
        val basicExp: Int,
        val levelExp: Int,
        val totalExp: Int,
        val currentLevel: Int,
        val nextLevelExp: Int,
        val currentLevelExp: Int
    )

    @Serializable
    data class TimeSeriesItem(
        val date: String,
        val count: Int,
        val rating: Double,
        val accuracy: Double,
        val cumulativeRating: Double,
        val cumulativeAccuracy: Double,
        val year: Int,
        val week: Int
    )

    @Serializable
    data class Tier(
        val name: String,
        val colorPalette: ColorPalette
    ) {
        @Serializable
        data class ColorPalette(
            val background: String
        )
    }

    @Serializable
    data class Character(
        val name: String,
        val variantName: String?,
        val exp: Exp
    ) {
        @Serializable
        data class Exp(
            val totalExp: Int,
            val currentLevel: Int,
            val nextLevelExp: Int,
            val currentLevelExp: Int
        )
    }

    companion object {
        fun get(cytoidID: String): ProfileWebapi {
            val response = OkHttpClient().newCall(
                Request.Builder()
                    .url("https://services.cytoid.io/profile/$cytoidID/details")
                    .removeHeader("User-Agent").addHeader("User-Agent", "CytoidClient/2.1.1")
                    .build()
            ).execute()
            val result = try {
                when (response.code) {
                    200 -> response.body?.string()
                    else -> throw Exception("Unknown Exception: HTTP response code is${response.code}")
                }
            } finally {
                response.body?.close()
            }
            return if (result == null) {
                throw Exception("Response body is null!HTTP response code is ${response.code}")
            } else {
                json.decodeFromString(result)
            }
        }
    }
}