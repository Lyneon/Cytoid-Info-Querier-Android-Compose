package com.lyneon.cytoidinfoquerier.data.model.webapi

import kotlinx.serialization.Serializable

@Serializable
data class LevelRating(
    val average: Double = 0.0,
    val total: Int = 0,
    val distribution: List<Int> = emptyList()
)