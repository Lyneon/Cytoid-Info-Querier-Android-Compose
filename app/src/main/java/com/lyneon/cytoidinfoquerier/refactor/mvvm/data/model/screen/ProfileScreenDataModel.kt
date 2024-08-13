package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.screen

import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileComment
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import kotlinx.serialization.Serializable

@Serializable
data class ProfileScreenDataModel(
    val profileDetails: ProfileDetails,
    val profileGraphQL: ProfileGraphQL,
    val commentList: List<ProfileComment>
)
