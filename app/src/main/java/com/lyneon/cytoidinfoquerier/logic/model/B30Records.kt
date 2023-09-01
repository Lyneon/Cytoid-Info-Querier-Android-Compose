package com.lyneon.cytoidinfoquerier.logic.model

import kotlinx.serialization.Serializable

@Serializable
data class B30Records(val data: Data) {
    @Serializable
    data class Data(val profile: Profile) {
        @Serializable
        data class Profile(
            val bestRecords: List<Record>
        ) {
            @Serializable
            data class Record(
                val date: String,
                val score: Int,
                val mods: List<String>,
                val accuracy: Float,
                val rating: Float,
                val details: Detail,
                val chart: Chart
            ) {
                @Serializable
                data class Detail(
                    val perfect: Int,
                    val great: Int,
                    val good: Int,
                    val bad: Int,
                    val miss: Int,
                    val maxCombo: Int
                )

                @Serializable
                data class Chart(
                    val type: String,
                    val difficulty: Int,
                    val notesCount: Int,
                    val level: Level
                ) {
                    @Serializable
                    data class Level(
                        val uid: String,
                        val title: String,
                        val bundle: Bundle
                    ) {
                        @Serializable
                        data class Bundle(
                            val backgroundImage: BackgroundImage
                        ) {
                            @Serializable
                            data class BackgroundImage(
                                val original: String,
                                val thumbnail: String
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun getRequestBody(playerName: String, count: Int): String = """{
  "operationName": null,
  "variables": {},
  "query": "{\n  profile(uid: \"${playerName}\") {\n    bestRecords(limit: ${count}) {\n      date\n      chart {\n        name\n        difficulty\n        type\n        notesCount\n        level {\n          uid\n          title\n          bundle {\n            backgroundImage {\n              original\n              thumbnail\n            }\n          }\n        }\n      }\n      score\n      accuracy\n      mods\n      details {\n        perfect\n        great\n        good\n        bad\n        miss\n        maxCombo\n      }\n      rating\n    }\n  }\n}\n"
}"""
    }
}