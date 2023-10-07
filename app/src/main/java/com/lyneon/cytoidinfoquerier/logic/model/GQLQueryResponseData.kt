package com.lyneon.cytoidinfoquerier.logic.model

import kotlinx.serialization.Serializable

@Serializable
data class GQLQueryResponseData<QueryType>(
    val data: QueryType
)