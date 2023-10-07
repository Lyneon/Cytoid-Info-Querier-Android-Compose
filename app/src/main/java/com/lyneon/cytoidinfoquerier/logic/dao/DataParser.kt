package com.lyneon.cytoidinfoquerier.logic.dao

import com.lyneon.cytoidinfoquerier.logic.model.Profile
import com.lyneon.cytoidinfoquerier.tool.DateParser
import com.lyneon.cytoidinfoquerier.tool.DateParser.formatToString
import com.lyneon.cytoidinfoquerier.tool.fix

object DataParser {
    fun parseProfileUserRecordToText(record: Profile.UserRecord): String =
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
}