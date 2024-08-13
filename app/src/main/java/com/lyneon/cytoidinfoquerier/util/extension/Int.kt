package com.lyneon.cytoidinfoquerier.util.extension

import com.lyneon.cytoidinfoquerier.data.constant.CytoidScoreRange

fun Int.isMaxCytoidGrade() = this == CytoidScoreRange.MAX

fun Int.isSSSCytoidGrade() = this in CytoidScoreRange.sss

fun Int.isSSCytoidGrade() = this in CytoidScoreRange.ss

fun Int.isSCytoidGrade() = this in CytoidScoreRange.s

fun Int.isAACytoidGrade() = this in CytoidScoreRange.aa

fun Int.isACytoidGrade() = this in CytoidScoreRange.a

fun Int.isBCytoidGrade() = this in CytoidScoreRange.b

fun Int.isCCytoidGrade() = this in CytoidScoreRange.c

fun Int.isDCytoidGrade() = this in CytoidScoreRange.d

fun Int.isFCytoidGrade() = this in CytoidScoreRange.f