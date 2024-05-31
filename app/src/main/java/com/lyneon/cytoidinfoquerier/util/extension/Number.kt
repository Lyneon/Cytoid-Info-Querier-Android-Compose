package com.lyneon.cytoidinfoquerier.util.extension

import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * 将数值按指定的保留的小数位数格式化为字符串
 *
 * @param digits 小数点后的保留位数
 * @param roundingMode 使用的近似模式
 */
fun <T : Number> T.setPrecision(
    digits: Int,
    roundingMode: RoundingMode = RoundingMode.CEILING
): String {
    val df = DecimalFormat("#.${"#" * digits}")
    df.roundingMode = roundingMode
    return df.format(this)
}