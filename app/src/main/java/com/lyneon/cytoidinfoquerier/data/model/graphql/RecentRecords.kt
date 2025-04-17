package com.lyneon.cytoidinfoquerier.data.model.graphql

import com.lyneon.cytoidinfoquerier.data.constant.RecordQueryOrder
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort
import com.lyneon.cytoidinfoquerier.data.model.graphql.type.UserRecord
import kotlinx.serialization.Serializable

@Serializable
data class RecentRecords(
    val data: RecentRecordsData,
    var queryArguments: RecentRecordsQueryArguments? = null
) {
    @Serializable
    data class RecentRecordsData(
        val profile: Profile? = null
    ) {
        @Serializable
        data class Profile(
            val user: User? = null,
            val recentRecords: List<UserRecord>
        ) {
            @Serializable
            data class User(
                val uid: String? = null
            )
        }
    }

    @Serializable
    data class RecentRecordsQueryArguments(
        val cytoidID: String,
        val recentRecordsLimit: Int,
        val recentRecordsSort: RecordQuerySort,
        val recentRecordsOrder: RecordQueryOrder
    )

    companion object {
        fun getRequestBodyString(
            cytoidID: String,
            recentRecordsLimit: Int = 0,
            recentRecordsSort: RecordQuerySort = RecordQuerySort.Date,
            recentRecordsOrder: RecordQueryOrder = RecordQueryOrder.DESC
        ) = """{
                profile(uid:"$cytoidID"){
                    user{
                        uid
                    }
                    recentRecords(limit:$recentRecordsLimit,sort:${recentRecordsSort.name},order:${recentRecordsOrder.name}){
                        ...UserRecord
                    }
                }
            }

            fragment UserRecord on UserRecord {
                score
                accuracy
                mods
                details {
                    perfect
                    great
                    good
                    bad
                    miss
                    maxCombo
                }
                rating
                date
                chart {
                    difficulty
                    type
                    name
                    notesCount
                    level {
                        uid
                        title
                        bundle {
                            backgroundImage {
                                thumbnail
                                original
                            }
                            music
                            musicPreview
                        }
                    }
                }
            }"""
    }
}