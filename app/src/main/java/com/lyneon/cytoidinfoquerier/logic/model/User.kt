package com.lyneon.cytoidinfoquerier.logic.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val uid: String,
    val registrationDate: String,
    val role: String,
    val lastSeen: String,
    val avatar: Avatar,
    val levelsCount: Int,
    val levels: ArrayList<UserLevel>,
    val collectionsCount: Int,
    val collections: ArrayList<CollectionUserListing>
) {
    @Serializable
    data class Avatar (
        val original:String
    )

    @Serializable
    data class UserLevel(
        val id: String,
        val uid: String
    )

    @Serializable
    data class CollectionUserListing(
        val id: String,
        val uid: String
    )

    fun getGQLQuery(uid: String):String = """{
  "operationName": null,
  "variables": {},
  "query": "{\n  user(uid: \"$uid\") {\n    id\n    uid\n    registrationDate\n    role\n    lastSeen\n    avatar {\n      original\n    }\n    levelsCount\n    levels {\n      id\n      uid\n    }\n    collectionsCount\n    collections {\n      id\n      uid\n    }\n  }\n}\n"
}"""
}