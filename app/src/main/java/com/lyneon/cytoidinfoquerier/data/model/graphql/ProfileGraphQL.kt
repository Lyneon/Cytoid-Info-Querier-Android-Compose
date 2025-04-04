package com.lyneon.cytoidinfoquerier.data.model.graphql

import com.lyneon.cytoidinfoquerier.data.model.graphql.type.UserRecord
import kotlinx.serialization.Serializable

@Serializable
data class ProfileGraphQL(
    val data: ProfileData
) {
    @Serializable
    data class ProfileData(
        val profile: Profile?
    ) {
        @Serializable
        data class Profile(
            val user: User?,
            val bio: String?,
            val badges: ArrayList<Badge>,
            val recentRecords: ArrayList<UserRecord>
        ) {
            @Serializable
            data class User(
                val id: String,
                val uid: String?,
                val registrationDate: String?,
                val lastSeen: String?,
                val avatar: Avatar,
                val levelsCount: Int,
                val levels: ArrayList<UserLevel>,
                val collectionsCount: Int,
                val collections: ArrayList<CollectionUserListing>
            ) {
                @Serializable
                data class Avatar(
                    val original: String?,
                    val large: String?
                )

                @Serializable
                data class UserLevel(
                    val uid: String,
                    val title: String,
                    val description: String,
                    val metadata: LevelMeta,
                    val bundle: LevelBundle?,
                    val charts: ArrayList<Chart>
                ) {
                    @Serializable
                    data class LevelMeta(
                        val artist: ResourceMetaProperty?
                    ) {
                        @Serializable
                        data class ResourceMetaProperty(
                            val name: String?
                        )
                    }

                    @Serializable
                    data class LevelBundle(
                        val music: String?,
                        val musicPreview: String? = null,
                        val backgroundImage: Image?
                    ) {
                        @Serializable
                        data class Image(
                            val original: String?,
                            val thumbnail: String?,
                            val cover: String?
                        )
                    }

                    @Serializable
                    data class Chart(
                        val name: String?,
                        val type: String,
                        val difficulty: Int,
                        val notesCount: Int
                    )
                }

                @Serializable
                data class CollectionUserListing(
                    val uid: String,
                    val title: String,
                    val slogan: String,
                    val levelCount: Int,
                    val cover: Image?
                ) {
                    @Serializable
                    data class Image(
                        val original: String?,
                        val thumbnail: String?,
                        val cover: String?
                    )
                }
            }

            @Serializable
            data class Badge(
                val title: String,
                val description: String?
            )
        }
    }

    companion object {
        fun getRequestBodyString(cytoidID: String) = """{
                profile(uid: "$cytoidID") {
                    user {
                        id
                        uid
                        registrationDate
                        lastSeen
                        avatar {
                            original
                            large
                        }
                        levelsCount
                        levels {
                            uid
                            title
                            description
                            metadata {
                                artist {
                                    name
                                }
                            }
                            bundle {
                                music
                                musicPreview
                                backgroundImage {
                                    original
                                    thumbnail
                                    cover
                                }
                            }
                            charts {
                                name
                                type
                                difficulty
                                notesCount
                            }
                        }
                        collectionsCount
                        collections {
                            uid
                            title
                            slogan
                            levelCount
                            cover {
                                original
                                thumbnail
                                cover
                            }
                        }
                    }
                    bio
                    badges {
                        title
                        description
                    }
                    recentRecords(limit: 10) {
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