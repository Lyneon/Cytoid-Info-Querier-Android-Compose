package com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql

import com.lyneon.cytoidinfoquerier.data.constant.RecordQueryOrder
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.type.UserRecord
import kotlinx.serialization.Serializable

@Serializable
data class RecentRecords(
    val data: RecentRecordsData
)  {
    @Serializable
    data class RecentRecordsData(
        val profile: Profile?
    ) {
        @Serializable
        data class Profile(
            val user: User?,
            val recentRecords: List<UserRecord>
        ) {
            @Serializable
            data class User(
                val uid: String?
            )
        }
    }

    companion object {
        fun getRequestBodyString(
            cytoidID: String,
            recentRecordsLimit: Int = 0,
            recentRecordsSort: String = RecordQuerySort.Date.name,
            recentRecordsOrder: String = RecordQueryOrder.DESC.name
        ) = """{
                profile(uid:"$cytoidID"){
                    user{
                        uid
                    }
                    recentRecords(limit:$recentRecordsLimit,sort:$recentRecordsSort,order:$recentRecordsOrder){
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