package com.lyneon.cytoidinfoquerier.data.constant

import okhttp3.OkHttpClient

object OkHttpSingleton {
    val instance: OkHttpClient by lazy {
        OkHttpClient()
    }
}