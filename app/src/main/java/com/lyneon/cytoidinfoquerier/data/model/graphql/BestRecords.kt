package com.lyneon.cytoidinfoquerier.data.model.graphql

import com.lyneon.cytoidinfoquerier.data.model.graphql.type.UserRecord
import kotlinx.serialization.Serializable

@Serializable
data class BestRecords(
    val data: BestRecordsData
) {
    @Serializable
    data class BestRecordsData(
        val profile: Profile? = null
    ) {
        @Serializable
        data class Profile(
            val user: User? = null,
            val bestRecords: List<UserRecord>
        ) {
            @Serializable
            data class User(
                val uid: String? = null
            )
        }
    }

    companion object {
        fun getRequestBodyString(
            cytoidID: String,
            bestRecordsLimit: Int = 0
        ) = """{
                profile(uid:"$cytoidID"){
                    user{
                        uid
                    }
                    bestRecords(limit:$bestRecordsLimit){
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