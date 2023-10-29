package com.lyneon.cytoidinfoquerier.logic.model

object CytoidDeepLink {
    fun getDeepLink(levelUID: String): String = "cytoid://levels/$levelUID"
}