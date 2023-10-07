package com.lyneon.cytoidinfoquerier.logic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LevelData(
    val level: Level
) {
    @Serializable
    data class Level(
        val id: Int,
        val uid: String,
        val version: Int,
        val title: String,
        val metadata: LevelMeta? = null,
        val duration: Float,
        val size: Int,
        val description: String? = null,
        val state: String,
        val tags: ArrayList<String>,
        val category: ArrayList<String>,
        val owner: User,
        val creationDate: String,
        val modificationDate: String,
        val bundle: LevelBundle,
        val charts: Chart
    ) {
        @Serializable
        data class LevelMeta(
            @SerialName("title_localized") val titleLocalized: String? = null,
            val artist: ResourceMetaProperty? = null,
            val illustrator: ResourceMetaProperty? = null,
            val charter: ResourceMetaProperty? = null,
            val storyboarder: ResourceMetaProperty? = null
        ) {
            @Serializable
            data class ResourceMetaProperty(
                val name: String? = null,
                @SerialName("localized_name") val localizedName: String? = null,
                val url: String? = null
            )
        }

        @Serializable
        data class User(
            val id: String,
            val uid: String
        )

        @Serializable
        data class LevelBundle(
            val music: String,
            val musicPreview: String? = null,
            val backgroundImage: Image
        ) {
            @Serializable
            data class Image(
                val original: String,
                val thumbnail: String
            )
        }

        @Serializable
        data class Chart(
            val type: String
        )

    }

    companion object {
        fun getGQLQuery(uid: String): String = """{
  "operationName": null,
  "variables": {},
  "query": "{\n  level(uid: \"$uid\") {\n    id\n    uid\n    version\n    title\n    metadata {\n      title_localized\n      artist {\n        ...ResourceMetaProperty\n      }\n      illustrator {\n        ...ResourceMetaProperty\n      }\n      charter {\n        ...ResourceMetaProperty\n      }\n      storyboarder {\n        ...ResourceMetaProperty\n      }\n    }\n    duration\n    size\n    description\n    state\n    tags\n    category\n    owner {\n      id\n      uid\n    }\n    creationDate\n    modificationDate\n    bundle {\n      music\n      musicPreview\n      backgroundImage {\n        original\n        thumbnail\n      }\n    }\n    charts {\n      type\n    }\n  }\n}\n\nfragment ResourceMetaProperty on ResourceMetaProperty {\n  name\n  localized_name\n  url\n}\n"
}"""
    }
}