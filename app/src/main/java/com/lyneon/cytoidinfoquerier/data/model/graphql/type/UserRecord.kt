package com.lyneon.cytoidinfoquerier.data.model.graphql.type

import com.lyneon.cytoidinfoquerier.util.DateParser
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

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
        @SerialName("difficulty") val difficultyLevel: Int,

        /**
         * easy，hard, extreme
         */
        @SerialName("type") val difficultyType: String,

        /**
         * 谱师自行指定的难度名称
         */
        @SerialName("name") val difficultyName: String?,
        val notesCount: Int,
        val level: RecordLevel?
    ) {
        @Serializable
        data class RecordLevel(
            val uid: String,
            val title: String,
            val bundle: LevelBundle?
        ) {
            @Serializable
            data class LevelBundle(
                val backgroundImage: Image?,
                val music: String?,
                val musicPreview: String?
            ) {
                @Serializable
                data class Image(
                    val thumbnail: String?,
                    val original: String?
                )
            }
        }
    }

    override fun toString(): String = StringBuilder().apply {
        val record = this@UserRecord
        record.chart?.let { recordChart ->
            appendLine("${recordChart.level?.title ?: "No LevelTitle"}(${
                recordChart.difficultyName
                    ?: recordChart.difficultyType.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
            } ${recordChart.difficultyLevel})(${recordChart.level?.uid ?: "No LevelUid"})")
        }
        appendLine(record.score)
        appendLine("${(record.accuracy * 100).setPrecision(2)}% accuracy  ${record.details.maxCombo} max combo")
        appendLine("Rating ${record.rating.setPrecision(2)}")
        appendLine("Perfect ${record.details.perfect} Great ${record.details.great} Good ${record.details.good} Bad ${record.details.bad} Miss ${record.details.miss}")
        appendLine(DateParser.parseISO8601Date(record.date).formatToTimeString())
    }.toString()
}