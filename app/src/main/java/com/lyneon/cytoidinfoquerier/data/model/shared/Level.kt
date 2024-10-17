package com.lyneon.cytoidinfoquerier.data.model.shared

import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val id: Int,
    val title: String,
    val uid: String,
    val description: String?,
    val artist: String?,
    val charter: String?,
    val illustrator: String?,
    val storyboarder: String?,
    val musicURL: String?,
    val musicPreviewURL: String?,
    val coverRemoteURL: String?,
    val charts: List<Chart>,
    val owner: Owner?,
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
        val difficultyName: String?,
        val notesCount: Int
    )

    @Serializable
    data class Owner(
        val uid: String?,
        val id: String,
        val avatar: Avatar
    ) {
        @Serializable
        data class Avatar(
            val original: String?,
            val small: String?,
            val medium: String?,
            val large: String?
        )
    }
}
