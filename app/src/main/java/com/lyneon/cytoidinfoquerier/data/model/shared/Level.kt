package com.lyneon.cytoidinfoquerier.data.model.shared

import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val id: Int,
    val title: String,
    val uid: String,
    val description: String? = null,
    val artist: String? = null,
    val charter: String? = null,
    val illustrator: String? = null,
    val storyboarder: String? = null,
    val musicURL: String? = null,
    val musicPreviewURL: String? = null,
    val coverRemoteURL: String? = null,
    val charts: List<Chart>,
    val owner: Owner? = null,
    val tags: List<String>,
    val category: List<String>,
    val creationDate: String,
    val modificationDate: String,
    val downloads: Int,
    val plays: Int
) {
    @Serializable
    data class Chart(
        val difficultyLevel: Int,
        val difficultyType: String,
        val difficultyName: String? = null,
        val notesCount: Int
    )

    @Serializable
    data class Owner(
        val uid: String? = null,
        val id: String,
        val avatar: Avatar
    ) {
        @Serializable
        data class Avatar(
            val original: String? = null,
            val small: String? = null,
            val medium: String? = null,
            val large: String? = null
        )
    }
}
