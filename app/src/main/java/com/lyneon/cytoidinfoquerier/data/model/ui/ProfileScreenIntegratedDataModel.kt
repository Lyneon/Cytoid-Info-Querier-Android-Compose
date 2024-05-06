package com.lyneon.cytoidinfoquerier.data.model.ui

import com.lyneon.cytoidinfoquerier.data.model.JSONDataModel
import com.lyneon.cytoidinfoquerier.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.data.model.webapi.Comment
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileWebapi
import kotlinx.serialization.Serializable

@Serializable
class ProfileScreenIntegratedDataModel(
    val queryTime: Long,
    val profileGraphQL: ProfileGraphQL,
    val profileWebapi: ProfileWebapi,
    val comments: List<Comment>
) : JSONDataModel()