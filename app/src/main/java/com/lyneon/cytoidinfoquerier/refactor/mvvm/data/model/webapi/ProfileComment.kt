package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val content: String,
    val date: String,
    val owner: Owner
) {
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
