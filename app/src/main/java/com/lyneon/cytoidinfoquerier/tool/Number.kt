package com.lyneon.cytoidinfoquerier.tool

import java.math.RoundingMode
import java.text.DecimalFormat

fun <T:Number> T.fix(digits: Int): String {
    val df = DecimalFormat("#.${"#" * digits}")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this)
}