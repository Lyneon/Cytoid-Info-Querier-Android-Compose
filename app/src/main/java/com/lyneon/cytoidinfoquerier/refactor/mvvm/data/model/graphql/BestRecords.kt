package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql

import kotlinx.serialization.Serializable

@Serializable
data class BestRecords(
    val data: BestRecordsData
) {
    @Serializable
    data class BestRecordsData(
        val profile: Profile?
    ) {
        @Serializable
        data class Profile(
            val bestRecords: List<UserRecord>
        )
    }
}