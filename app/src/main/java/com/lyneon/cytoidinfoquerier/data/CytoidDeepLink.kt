package com.lyneon.cytoidinfoquerier.data

object CytoidDeepLink {
    fun getCytoidLevelDeepLink(levelUID: String): String = "cytoid://levels/$levelUID"
}