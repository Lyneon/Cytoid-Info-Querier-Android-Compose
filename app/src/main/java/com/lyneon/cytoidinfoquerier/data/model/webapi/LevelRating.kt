package com.lyneon.cytoidinfoquerier.data.model.webapi

import kotlinx.serialization.Serializable

@Serializable
data class LevelRating(
    val average: Double = -1.0,
    val total: Int = -1,
    val distribution: List<Int> = emptyList()
)