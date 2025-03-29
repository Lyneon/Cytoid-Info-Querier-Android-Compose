package com.lyneon.cytoidinfoquerier.data.model.webapi

import kotlinx.serialization.Serializable

@Serializable
data class LevelComment(
    val id: Int,
    val content: String,
    val date: String,
    val owner: Owner? = null
) {
    @Serializable
    data class Owner(
        val id: String,
        val uid: String?,
        val avatar: Avatar
    ) {
        @Serializable
        data class Avatar(
            val original: String?,
            val large: String?,
            val medium: String?,
            val small: String?
        )
    }
}
