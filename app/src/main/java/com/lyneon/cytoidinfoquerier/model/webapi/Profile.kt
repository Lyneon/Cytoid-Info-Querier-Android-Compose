package com.lyneon.cytoidinfoquerier.model.webapi

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
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
            val original: String
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
}