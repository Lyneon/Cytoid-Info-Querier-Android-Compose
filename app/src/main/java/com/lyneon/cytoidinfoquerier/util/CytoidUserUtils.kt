package com.lyneon.cytoidinfoquerier.util

import com.lyneon.cytoidinfoquerier.data.repository.ProfileDetailsRepository

object CytoidUserUtils {
    suspend fun getUserIdByCytoidId(cytoidId: String): String =
        ProfileDetailsRepository().getProfileDetails(cytoidId).user.id
}