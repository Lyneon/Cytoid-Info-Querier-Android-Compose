package com.lyneon.cytoidinfoquerier.logic.dao

import com.lyneon.cytoidinfoquerier.logic.model.B30Records
import com.lyneon.cytoidinfoquerier.logic.model.PlayerProfile
import com.lyneon.cytoidinfoquerier.tool.DateParser
import com.lyneon.cytoidinfoquerier.tool.DateParser.formatToString
import com.lyneon.cytoidinfoquerier.tool.fix

object DataParser {
    fun parsePlayerProfileToText(profile: PlayerProfile, isDetail: Boolean): String =
        if (!isDetail) {
            try {
                StringBuilder().apply {
                    appendLine("${profile.user.uid}(等级${profile.exp.currentLevel})")
                    appendLine("Rating：${profile.rating.toDouble().fix(2)}")
                    appendLine("总游玩次数(非练习模式): ${profile.activities.totalRankedPlays}")
                    appendLine("总击中note: ${profile.activities.clearedNotes}")
                    appendLine("最大连击数: ${profile.activities.maxCombo}")
                    appendLine("平均精准度: ${(profile.activities.averageRankedAccuracy * 100).fix(2)}%")
                    append("总分数: ${profile.activities.totalRankedScore}")
                    //appendLine("总游玩时长: ${activities.getFloat("totalPlayTime")}s")
                }.toString()
            } catch (e: Exception) {
                throw e
            }
        } else {
            try {
                StringBuilder().apply {
                    appendLine("${profile.user.uid}(等级${profile.exp.currentLevel})(${profile.tier?.name})")
                    appendLine("Rating: ${profile.rating.toDouble().fix(2)}")
                    appendLine("总游玩次数(非练习模式): ${profile.activities.totalRankedPlays}")
                    appendLine("总击中note: ${profile.activities.clearedNotes}")
                    appendLine("最大连击数: ${profile.activities.maxCombo}")
                    appendLine("平均精准度: ${(profile.activities.averageRankedAccuracy * 100).fix(2)}%")
                    appendLine("总分数: ${profile.activities.totalRankedScore}")
                    appendLine("成绩：${profile.grade}")
                    appendLine("角色：${profile.character.name}(等级${profile.character.exp.currentLevel})")
                    appendLine("最近成绩：")
                    for (record in profile.recentRecords) {
                        appendLine("${record.chart.level.title}(${record.chart.type} ${record.chart.difficulty})")
                        appendLine(record.score)
                        appendLine(
                            if (record.score == 1000000) "All Perfect"
                            else if (record.details.maxCombo == record.chart.notesCount) "Full Combo 全连击"
                            else "${record.details.maxCombo} 最大连击"
                        )
                        appendLine("精准度：${((record.accuracy) * 100).fix(2)}%")
                        appendLine(record.details.toString())
                        appendLine()
                    }
                }.toString()
            } catch (e: Exception) {
                throw e
            }
        }

    fun parseB30RecordToText(record: B30Records.Data.Profile.Record): String =
        StringBuilder().apply {
            appendLine("${record.chart.level.title}(${record.chart.type} ${record.chart.difficulty})")
            appendLine(record.score)
            appendLine(
                if (record.score == 1000000) "All Perfect"
                else if (record.details.maxCombo == record.chart.notesCount) "Full Combo 全连击"
                else "最大连击 ${record.details.maxCombo}"
            )
            appendLine("Mods：${record.mods}")
            appendLine("精准度：${((record.accuracy) * 100).fix(2)}%")
            appendLine("单曲Rating：${record.rating}")
            appendLine(record.details.toString())
            appendLine()
            append(DateParser.parseISO8601Date(record.date).formatToString())
        }.toString()

    fun parseB30RecordsToText(b30Records: B30Records, playerName: String): String =
        try {
            StringBuilder().apply {
                appendLine("${playerName}的${b30Records.data.profile.bestRecords.size}条最佳游玩记录：")
                for (record in b30Records.data.profile.bestRecords) {
                    parseB30RecordToText(record)
                    appendLine()
                }
            }.toString()
        } catch (e: Exception) {
            throw e
        }
}