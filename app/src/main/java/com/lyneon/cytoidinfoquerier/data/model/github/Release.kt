package com.lyneon.cytoidinfoquerier.data.model.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Release(
    val name: String,
    @SerialName("published_at") val publishDate: String,
    val assets: List<Asset>,
    val body: String?
) {
    @Serializable
    data class Asset(
        val name: String,
        val size: Long,
        @SerialName("download_count") val downloadCount: Long,
        @SerialName("updated_at") val updateDate: String,
        @SerialName("browser_download_url") val downloadUrl: String
    )
}
