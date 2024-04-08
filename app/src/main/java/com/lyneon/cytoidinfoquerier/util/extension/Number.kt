package com.lyneon.cytoidinfoquerier.util.extension

import java.math.RoundingMode
import java.text.DecimalFormat

fun <T : Number> T.setPrecision(
    digits: Int,
    roundingMode: RoundingMode = RoundingMode.CEILING
): String {
    val df = DecimalFormat("#.${"#" * digits}")
    df.roundingMode = roundingMode
    return df.format(this)
}