package com.lyneon.cytoidinfoquerier.data.model.webapi

import com.lyneon.cytoidinfoquerier.data.model.shared.Level
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchLevelsResult(
    val id: Int,
    val version: Int,
    val uid: String,
    val title: String,
    val metadata: LevelMetadata,
    val duration: Float = -1f,
    val size: Int = -1,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val category: List<String>,
    val creationDate: String,
    val modificationDate: String,
    val owner: User? = null,
    val plays: Int = -1,
    val downloads: Int = -1,
    val charts: List<LevelChart>,
    val state: String,
    val cover: Image? = null,
    val music: String? = null,
    val musicPreview: String? = null
) {
    @Serializable
    data class LevelMetadata(
        val title: String? = null,
        @SerialName("title_localized") val localizedTitle: String? = null,
        val artist: ResourceMetaProperty? = null,
        val charter: ResourceMetaProperty? = null,
        val illustrator: ResourceMetaProperty? = null,
        val storyboarder: ResourceMetaProperty? = null
    ) {
        @Serializable
        data class ResourceMetaProperty(
            val name: String? = null,
            @SerialName("localized_name") val localizedName: String? = null,
            val url: String? = null
        )
    }

    @Serializable
    data class User(
        val id: String,
        val uid: String? = null,
        val name: String? = null,
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

    @Serializable
    data class LevelChart(
        val id: Int,
        @SerialName("difficulty") val difficultyLevel: Int,
        @SerialName("name") val difficultyName: String? = null,
        val notesCount: Int,
        @SerialName("type") val difficultyType: String
    )

    @Serializable
    data class Image(
        val original: String? = null,
        val thumbnail: String? = null,
        val cover: String? = null,
        val stripe: String? = null
    )

    fun toSharedLevel() = Level(
        id = this.id,
        title = this.title,
        uid = this.uid,
        description = this.description,
        artist = this.metadata.artist?.name,
        charter = this.metadata.charter?.name,
        illustrator = this.metadata.illustrator?.name,
        storyboarder = this.metadata.storyboarder?.name,
        musicURL = this.music,
        musicPreviewURL = this.musicPreview,
        charts = this.charts.map {
            Level.Chart(
                it.difficultyLevel,
                it.difficultyType,
                it.difficultyName,
                it.notesCount
            )
        },
        coverRemoteURL = this.cover?.original,
        owner = if (this.owner == null) null else Level.Owner(
            this.owner.uid,
            this.owner.id,
            Level.Owner.Avatar(
                this.owner.avatar.original,
                this.owner.avatar.small,
                this.owner.avatar.medium,
                this.owner.avatar.large
            )
        ),
        tags = this.tags,
        category = this.category,
        creationDate = this.creationDate,
        modificationDate = this.modificationDate,
        plays = this.plays,
        downloads = this.downloads
    )
}