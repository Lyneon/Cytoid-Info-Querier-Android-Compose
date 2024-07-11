package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql

import kotlinx.serialization.Serializable

@Serializable
data class RecentRecords(
    val data: RecentRecordsData
) {
    @Serializable
    data class RecentRecordsData(
        val profile: Profile?
    ) {
        @Serializable
        data class Profile(
            val recentRecords: List<UserRecord>
        )
    }
}