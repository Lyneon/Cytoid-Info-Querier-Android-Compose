package com.lyneon.cytoidinfoquerier.data.model.ui

import com.lyneon.cytoidinfoquerier.data.model.JSONDataModel
import com.lyneon.cytoidinfoquerier.data.model.graphql.Analytics
import kotlinx.serialization.Serializable

@Serializable
class AnalyticsScreenDataModel(
    val analytics: Analytics
) : JSONDataModel()