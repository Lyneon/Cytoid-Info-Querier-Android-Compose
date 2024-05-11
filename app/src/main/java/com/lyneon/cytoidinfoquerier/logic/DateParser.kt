package com.lyneon.cytoidinfoquerier.logic

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateParser {
    fun parseISO8601Date(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.parse(dateString) as Date
    }

    /**
     * 将当前Date对象转换为字符串表示
     * @param pattern 目标字符串的期望格式
     * @return Date对象的字符串表示
     */
    fun Date.formatToTimeString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(this)
    }

    fun Long.timeStampToString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String =
        Date(this).formatToTimeString(pattern)
}