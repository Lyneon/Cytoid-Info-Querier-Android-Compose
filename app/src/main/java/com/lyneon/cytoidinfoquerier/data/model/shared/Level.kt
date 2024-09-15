package com.lyneon.cytoidinfoquerier.data.model.shared

import kotlinx.serialization.Serializable

@Serializable
data class Level(
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
    val charts: List<Chart>
) {
    @Serializable
    data class Chart(
        val difficultyLevel: Int,
        val difficultyType: String,
        val difficultyName: String?,
        val notesCount: Int
    )
}
