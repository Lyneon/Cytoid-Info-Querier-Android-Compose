package com.lyneon.cytoidinfoquerier.data.model.graphql

import com.lyneon.cytoidinfoquerier.data.constant.RecordQueryOrder
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort
import com.lyneon.cytoidinfoquerier.util.DateParser
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

@Serializable
data class Analytics(
    val data: Data
) : GraphQLJSONDataModel() {
    @Serializable
    data class Data(
        val profile: Profile?
    ) {
        @Serializable
        data class Profile(
            var recentRecords: ArrayList<UserRecord>,
            var bestRecords: ArrayList<UserRecord>
        )
    }

    companion object {
        fun getQueryString(
            cytoidID: String,
            recentRecordsLimit: Int = 0,
            recentRecordsSort: String = RecordQuerySort.Date.name,
            recentRecordsOrder: String = RecordQueryOrder.DESC.name,
            bestRecordsLimit: Int = 0
        ) = """{
                profile(uid:"$cytoidID"){
                    recentRecords(limit:$recentRecordsLimit,sort:$recentRecordsSort,order:$recentRecordsOrder){
                        ...UserRecord
                    },
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

        fun decodeFromJSONString(json: String): Analytics {
            val jsonHandler = Json { ignoreUnknownKeys = true }
            return jsonHandler.decodeFromString(json)
        }
    }
}

@Serializable
data class UserRecord(
    val score: Int,
    val accuracy: Float,
    val mods: ArrayList<String>,
    val details: RecordDetails,
    val rating: Float,
    val date: String,
    val chart: RecordChart?
) {
    fun detailsString(): String = StringBuilder().apply {
        val record = this@UserRecord
        record.chart?.let {
            appendLine("${record.chart.level?.title ?: "LevelTitle"}(${
                record.chart.name
                    ?: record.chart.type.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
            } ${record.chart.difficulty})(${record.chart.level?.uid ?: "LevelUid"})")
        }
        appendLine(record.score)
        appendLine("${(record.accuracy * 100).setPrecision(2)}% accuracy  ${record.details.maxCombo} max combo")
        appendLine("Rating ${record.rating.setPrecision(2)}")
        appendLine("Perfect ${record.details.perfect} Great ${record.details.great} Good ${record.details.good} Bad ${record.details.bad} Miss ${record.details.miss}")
        appendLine(DateParser.parseISO8601Date(record.date).formatToTimeString())
    }.toString()

    @Serializable
    data class RecordDetails(
        val perfect: Int,
        val great: Int,
        val good: Int,
        val bad: Int,
        val miss: Int,
        val maxCombo: Int
    )

    @Serializable
    data class RecordChart(
        val difficulty: Int,
        val type: String,
        val name: String?,
        val notesCount: Int,
        val level: RecordLevel?
    ) {
        @Serializable
        data class RecordLevel(
            val uid: String,
            val title: String,
            val bundle: LevelBundle
        ) {
            @Serializable
            data class LevelBundle(
                val backgroundImage: Image,
                val music: String,
                val musicPreview: String? = null
            ) {
                @Serializable
                data class Image(
                    val thumbnail: String,
                    val original: String
                )
            }
        }
    }
}