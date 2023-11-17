package com.lyneon.cytoidinfoquerier.model

object CytoidDeepLink {
    fun getDeepLink(levelUID: String): String = "cytoid://levels/$levelUID"
}