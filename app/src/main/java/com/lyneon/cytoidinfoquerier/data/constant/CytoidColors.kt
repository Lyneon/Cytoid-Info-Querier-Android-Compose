package com.lyneon.cytoidinfoquerier.data.constant

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object CytoidColors {
    val easyColor = listOf(Color(0xff67b26f), Color(0xff4ca2cd))
    val hardColor = listOf(Color(0xff4568dc), Color(0xffb06abc))
    val extremeColor = listOf(Color(0xff200122), Color(0xff6f0000))
    val accentColor = Color(165, 180, 252)
    val backgroundColor = Color(39, 41, 53)
    val sssColor = listOf(Color(0xffffc53d), Color(0xffff5e07))
    val maxColor = listOf(Color(0xffec00c6), Color(0xff0096ff))
    val featuredColor = listOf(Color(0xffdf3090), Color(0xfff953c6))
    val qualifiedColor = listOf(Color(0xff1d976c), Color(0xff1cb068))
    val perfectColor = Color(0xff60a5fa)
    val greatColor = Color(0xfffacc15)
    val goodColor = Color(0xff4ade80)
    val badColor = Color(0xfff87171)
    val missColor = Color(0xff94a3b8)
}

fun List<Color>.toIntList() = this.map { it.toArgb() }

fun List<Color>.toIntArray() = this.toIntList().toIntArray()