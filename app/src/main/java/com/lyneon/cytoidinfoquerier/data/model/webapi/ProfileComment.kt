package com.lyneon.cytoidinfoquerier.data.model.webapi

import kotlinx.serialization.Serializable

@Serializable
data class ProfileComment(
    val content: String,
    val date: String,
    val owner: Owner
)  {
    @Serializable
    data class Owner(
        val uid: String,
        val avatar: Avatar
    ) {
        @Serializable
        data class Avatar(
            val original: String,
            val small: String,
            val medium: String,
            val large: String
        )
    }
}
