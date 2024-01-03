package com.lyneon.cytoidinfoquerier.model

object CytoidDeepLink {
    fun getCytoidLevelDeepLink(levelUID: String): String = "cytoid://levels/$levelUID"
}