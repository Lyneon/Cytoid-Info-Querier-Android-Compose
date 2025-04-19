package com.lyneon.cytoidinfoquerier.data.model.local

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsPreset(
    val name: String,
    val queryType: String,
    val queryCount: Int,
    val querySort: String,
    val queryOrder: String,
    val ignoreLocalCacheData: Boolean,
    val keep2DecimalPlaces: Boolean,
    val imageGenerationColumns: Int = 6
)
