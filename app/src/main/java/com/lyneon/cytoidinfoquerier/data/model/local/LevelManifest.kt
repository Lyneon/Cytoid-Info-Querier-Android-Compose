package com.lyneon.cytoidinfoquerier.data.model.local

import kotlinx.serialization.Serializable

@Serializable
data class LevelManifest(
    val version: Int,
    val id: String,
    val title: String,
    val artist: String? = null,
    val illustrator: String? = null,
    val charter: String? = null,
    val music: Resource? = null,
    val background: Resource? = null,
    val charts: List<Chart>
) {
    @Serializable
    data class Resource(
        val path: String? = null
    )

    @Serializable
    data class Chart(
        val type: String,
        val name: String? = null,
        val difficulty: Int,
        val path: String,
        val storyboard: Resource? = null
    )
}
